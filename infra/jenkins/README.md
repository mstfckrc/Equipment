# Yerel Jenkins CI

Kök `compose.yaml` içindeki `jenkins` ve `jenkins-agent`, `ci` profilindedir.
Normal `docker compose up -d --build` komutu bu iki bileşeni başlatmaz.
İlk pipeline checkout ve Maven paketlemesi yapar; test çalıştırma ve CI
PostgreSQL'i bu başlangıç kontrolüne dahil değildir.

## 1. Controller'ı başlat

Komutları proje kökündeki PowerShell terminalinde çalıştır:

```powershell
docker compose up -d jenkins
docker compose ps jenkins
```

Servisi adıyla seçmek profilini etkinleştirir; uygulama servislerini başlatmaz.
İlk açılış biraz sürebilir. Jenkins adresi: <http://localhost:8081>.
`JENKINS_PORT` değiştirildiyse adresin portunu da değiştir.

İlk kurulum ekranının istediği parolayı yalnız kendi terminalinde görüntüle:

```powershell
docker compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Bu değer ve diğer credential'lar paylaşılmaz. Parolayı Jenkins ekranına gir,
`Select plugins to install` ile `Pipeline`, `Git`, `JUnit` ve
`Credentials Binding` eklentilerini ve bağımlılıklarını kur. Yönetici hesabını
oluştur. Jenkins URL alanı `http://localhost:8081/` olmalı.

`Jenkins'i Yönet → Eklentiler → Yüklenmiş eklentiler` bölümünde `Pipeline`,
`Git`, `JUnit` ve `Credentials Binding` eklentilerinin kurulu ve `Devrede`
olduğunu kontrol et.

Türkçe Jenkins 2.580.2 arayüzündeki `Jenkins'i Yönet → Sistem Konfigürasyonu →
Sunucular ve Bulutlar` listesindeki controller satırı `master` adını taşır.
Bu ad aynı sürümün Türkçe kaynaklarında doğrulandı. Controller ayarındaki
`Yapılandır` eylemi `Configure`,
`Yapılandırma işlemleri sayısı` alanı `Number of executors` karşılığıdır.
Bu alanda hedef değer `0` olur; kaydetme eylemi `Kaydet` olarak çevrilir.

Türkçe kaynakta `Hudson.Computer.DisplayName=master` bulunur. İngilizce
belgelerdeki `Built-In Node` adını bu kurulumun ekranında aramak doğru değildir.
Menü etiketleri sürüm, dil ve eklentiye bağlıdır; çeviri eksik olduğunda bazı
alanlar İngilizce kalır. Aşağıdaki form bilgileri ilgili sürüm kaynaklarıyla
eşleştirilmiştir; runtime işlemleri kullanıcı tarafından ayrıca doğrulanır.

## 2. Private GitHub reposu için credential ekle

GitHub'da süreli bir fine-grained PAT oluştur: yalnız EquipmentHub reposunu
seç, repository permission olarak `Contents: Read-only` ver. İlk CI için
repo yazma veya webhook yönetme yetkisi gerekmez.

`Manage Jenkins → Credentials → System → Global credentials (unrestricted) → Add Credentials`:

| Alan | Değer |
| --- | --- |
| Kind | Username with password |
| Scope | Global |
| Username | GitHub kullanıcı adın |
| Treat username as secret | İşaretleme; bu projede GitHub kullanıcı adı gizli değil |
| Password | Yalnız bu repoya Contents: Read-only erişimli, süreli GitHub PAT |
| ID | equipmenthub-github-read |
| Description | EquipmentHub private repository checkout |

PAT `.env` veya kaynak koda eklenmez. Multibranch Pipeline'ın GitHub branch
source'ında bu credential seçilecek; henüz checkout veya pipeline doğrulanmış
değildir.

## 3. Agent node'unu oluştur

`Jenkins'i Yönet → Sistem Konfigürasyonu → Sunucular ve Bulutlar` sayfasındaki
`Yeni Sunucu` düğmesi yeni agent formunu açar. İlk ekrandaki alan
`Sunucu ismi`, tür seçeneği `Kalıcı Ajan` olarak çevrilmiştir.

| Alan | Değer |
| --- | --- |
| Sunucu ismi (Node name) | equipmenthub-agent |
| Tür | Kalıcı Ajan (Permanent Agent) |
| Number of executors | 1 |
| Remote root directory | /home/jenkins/agent |
| Labels | equipmenthub-ci |
| Kullanım (Usage) | Only build jobs with label expressions matching this node |
| Launch method | Launch agent by connecting it to the controller |
| Availability | Keep this agent online as much as possible |

WebSocket için node formunda ayrıca bir kutu aranmaz. Compose'taki agent
servisinde `JENKINS_WEB_SOCKET=true` bulunur; inbound agent bu ayarla WebSocket
üzerinden bağlanır.

`Number of executors` alanının controller formundaki Türkçe çevirisini
agent formuna otomatik uygulama: bunlar ayrı kaynak dosyalarıdır. Bu sürümde
agent'ın `configure-entries` formunun Türkçe dosyası bulunmaz; burada İngilizce
alan adları korunmuştur. Ek Node Properties/Advanced ayarları varsayılanda kalır.

Kaydet ve node sayfasını aç. Gösterilen agent secret'ını kökteki takip
edilmeyen `.env` dosyasında `JENKINS_AGENT_SECRET=` alanına yaz. Bu değer,
GitHub PAT veya uygulamanın JWT sırrı değildir. Agent, Compose ağındaki
`http://jenkins:8080` adresine WebSocket ile bağlanır; `50000` portu açılmaz.

Secret doldurulduktan sonra:

```powershell
docker compose up -d jenkins-agent
docker compose ps jenkins jenkins-agent
```

Jenkins node ekranında agent'ın çevrimiçi olması bağlantı kontrolüdür.
Gerekirse yalnız kendi terminalinde `docker compose logs --tail 50 jenkins-agent`
ile bağlantı hatasını incele; paylaşmadan önce sır içermediğini kontrol et.
Node'un `Sistem Bilgisi` ekranında Java 25 görülmeli.

## Kaynaklar ve günlük kullanım

- Sabit image sürümleri: `jenkins/jenkins:2.580.2-jdk25` ve
  `jenkins/inbound-agent:3395.v6e5377b_0fb_3c-4-jdk25`.
- Resmî agent JDK 25, Git ve curl içerir; özel Dockerfile gerekmiyor.
- Controller: en fazla 2 GiB RAM / 1 CPU; agent: 3 GiB RAM / 2 CPU.
  Bunlar üst sınırlardır, ayrılmış bellek miktarı değildir. Docker Desktop/WSL
  toplam bütçesi uygulama container'larıyla paylaşılır; gerçek kullanım ilk
  build sırasında değerlendirilecek. Maven/test alt süreçleri de agent limitine dahildir.
- Jenkins ayarları, credential'lar ve geçmiş `jenkins-home`; agent çalışma
  alanı ve Maven cache `jenkins-agent-work` volume'ünde korunur.
- CI bileşenleri ayrı `ci` ağındadır; uygulama ağına veya Docker socket'a bağlı değildir.
- İlk kurulumda agent secret'ı boşken bütün `ci` profilini başlatma.
  Kurulumdan sonra ikisini birlikte başlatmak için:

```powershell
docker compose up -d jenkins jenkins-agent
```

Yalnız CI'ı durdurmak için:

```powershell
docker compose stop jenkins-agent jenkins
```

`on-failure:3` hata sonrası sınırlı yeniden deneme sağlar; Docker Desktop
açıldığında otomatik başlatma politikası değildir. `docker compose down -v`
Jenkins dahil proje volume'lerini ve uygulama verilerini silebilir; durdurmak
için yukarıdaki `stop` komutunu kullan.

İlk pipeline'da `-DskipTests package` kullanılır. Build'in başarılı olması
testlerin geçtiğini göstermez. Otomatik testler, raporlar ve gerekirse CI
PostgreSQL'i test kapsamı gerektiğinde eklenir. Controller/agent'ın çalışması
da tek başına başarılı CI, image build veya deployment kanıtı değildir.

Resmî kaynaklar: [Controller image](https://github.com/jenkinsci/docker),
[Agent kurulumu](https://github.com/jenkinsci/docker-agents/blob/master/README_inbound-agent.md),
[Git credentials](https://plugins.jenkins.io/git/#using-credentials),
[GitHub PAT](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens).

Arayüz adları için sürüme sabitlenmiş kaynaklar (2026-11-07'de incelendi):
[Controller adı](https://github.com/jenkinsci/jenkins/blob/jenkins-2.580.2/core/src/main/resources/hudson/model/Messages_tr.properties),
[Controller formu](https://github.com/jenkinsci/jenkins/blob/jenkins-2.580.2/core/src/main/resources/jenkins/model/Jenkins/MasterComputer/configure_tr.properties),
[Sunucular listesi](https://github.com/jenkinsci/jenkins/blob/jenkins-2.580.2/core/src/main/resources/hudson/model/ComputerSet/index_tr.properties),
[Yeni sunucu formu](https://github.com/jenkinsci/jenkins/blob/jenkins-2.580.2/core/src/main/resources/hudson/model/ComputerSet/new_tr.properties),
[Agent türü](https://github.com/jenkinsci/jenkins/blob/jenkins-2.580.2/core/src/main/resources/hudson/slaves/Messages_tr.properties),
[Agent formu](https://github.com/jenkinsci/jenkins/blob/jenkins-2.580.2/core/src/main/resources/hudson/slaves/DumbSlave/configure-entries.jelly),
[WebSocket alanı](https://github.com/jenkinsci/jenkins/blob/jenkins-2.580.2/core/src/main/resources/hudson/slaves/JNLPLauncher/config.jelly),
[Credentials formu](https://github.com/jenkinsci/credentials-plugin/blob/1514.vb_01eb_014e7d4/src/main/resources/com/cloudbees/plugins/credentials/impl/UsernamePasswordCredentialsImpl/credentials.jelly).

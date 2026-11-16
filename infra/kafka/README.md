# Yerel Kafka

Compose, Apache Kafka 4.4.0'ı tek broker/controller KRaft olarak çalıştırır.
Altyapı CLI deneyleriyle doğrulandı. Rental'ın otomatik kiralama onay belgesi
entegrasyonu Compose ortamında, kesinti deneyleri ve gerçek tarayıcı akışıyla doğrulandı.

Proje kökünde:

```powershell
docker compose up -d kafka
docker compose ps kafka
```

Başarı ölçütü `healthy` durumudur. Gerekirse son loglar:
`docker compose logs --tail 80 kafka`.

| Bağlantı | Adres |
|---|---|
| Docker ağındaki client | `kafka:9092` |
| Host ortamındaki client | `localhost:19092` |

Client, başlangıç adresinden sonra broker'ın advertised listener adresini
kullanır; bu nedenle Docker'daki servise `localhost:19092` verilmez.
Controller portu host'a açılmaz. PLAINTEXT ve tek node yerel geliştirme
seçimleridir; production güvenliği veya yüksek erişilebilirlik sağlamaz.

## Öğrenme topic'i

Broker healthy olduktan sonra topic açıkça oluşturulur; otomatik topic açma kapalıdır.
Bu komut yeniden çalıştırılabilir; var olan topic'in ayarlarını değiştirmez.
Retention ayarları yalnız bu öğrenme topic'ine aittir.

```powershell
docker compose exec -e KAFKA_HEAP_OPTS="-Xms32m -Xmx128m" kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --create --if-not-exists --topic equipmenthub.learning.v1 --partitions 3 --replication-factor 1 --config retention.ms=86400000 --config retention.bytes=268435456 --config segment.bytes=67108864 --config segment.ms=3600000
```

- **Producer/consumer:** Kafka CLI ile anahtarlı birkaç olay gönderip key,
  partition, offset ve değeri birlikte okuyacağız. Sıralama partition içindedir.
- **Consumer kapalıyken gönderme:** sabit group adıyla kaldığı yerden okuyacak.
- **Gruplar:** aynı gruptaki iki consumer üç partition'ı paylaşacak; farklı
  grup aynı olayları kendi offset'leriyle okuyacak. `--from-beginning`, offset'i
  zaten kaydedilmiş bir grubu otomatik sıfırlamaz.
- **Restart:** aynı volume ile kontrollü broker restart sonrası mesajlar ve
  kaydedilmiş group offset'leri doğrulanacak. Normal restart veri silmez.

`kafka-data`, mesajları ve KRaft metadata'sını tutar. `CLUSTER_ID` sır değildir;
mevcut volume kullanılırken değiştirilmez. Volume silme/sıfırlama ayrıca onay
gerektirir. Topic retention yaklaşık bir gündür; boyut sınırı partition başına
256 MiB'dir. Silme segmentlerle ve periyodik çalıştığından anlık disk kotası değildir.
Healthcheck/CLI için küçük heap kullanılır; broker heap'i en fazla 512 MiB,
container sınırı 1 GiB/1 CPU'dur. İleride yük ve replika ihtiyaçları ayrıca ölçülür.

## Doğrulama — 2026-11-15

Dört deney gerçek broker üzerinde geçti. Aynı `reservation-101` anahtarlı
üç olay partition 1 / offset 0–2 sırasıyla okundu. Consumer kapalıyken eklenen
iki olay aynı grupta offset 3–4'ten devam etti. İki consumer aynı grupta
partition'ları 2+1 paylaştı ve toplam 17 olayı 13+4 olarak işledi; lag sıfırdı.
Bağımsız grup 17 olayın tamamını okuyabildi.

Broker restart sonrasında partition sonları 3/10/4 ve eski grubun kaydedilmiş
offset'leri 0/5/0 korundu; grup yalnız kalan 12 olayı okudu. Yeni replay grubu
17 olayı baştan okuyabildi. EXTERNAL listener CLI ile, host portu TCP ile
doğrulandı. Volume ve test kayıtları silinmedi; consumer süreçleri kapandı.
Bu sonuç uygulama iş olaylarının veya production dayanıklılığının testi değildir.

Kafka 4.4 CLI'sinde producer ayarları `--command-property`, key okuma ayarları
`--reader-property`, consumer çıktı ayarları `--formatter-property` ile verilir.
Grup paylaşımı deneyindeki `--timeout-ms 45000`, boşta kalan CLI consumer'ı
bilinçli sonlandırır; konsolda görülen bu `TimeoutException` broker hatası değildir.

Kaynaklar: [Kafka Docker örneği](https://github.com/apache/kafka/blob/4.4.0/docker/examples/docker-compose-files/single-node/plaintext/docker-compose.yml),
[Quickstart](https://kafka.apache.org/quickstart/).

## Rental onay belgesi — 2026-11-16

Checkout aynı Rental DB transaction'ında rezervasyonları, checkout başına tek
PENDING belgeyi ve outbox olayını kaydeder. Kafka'ya bağlantı kullanıcı isteğinde
yapılmaz. Yayıncı `KafkaTemplate` ile broker onayını bekler; başarısız gönderimleri
2–60 saniyelik beklemeyle tekrar dener. `@KafkaListener` onay anında kaydedilmiş
ürün/tarih/fiyat verilerinden JSONB belgeyi üretir. Mağaza adları mevcut public
Store API'sinden hazırlama sırasında alınır; görünür olmayan mağaza için kimlik
referansı kullanılır. Hazır belge tekrar üretilmez veya iptalle değiştirilmez.

- Ana topic: `equipmenthub.rental.checkout-confirmed.v1` (3 partition, RF1).
- Hata topic'i: ana topic + `.dlt` (3 partition, RF1).
- Key: checkout UUID; consumer group: `rental-confirmation-v1`.
- Saklama: ana topic 7, DLT 14 gün; her partition en fazla yaklaşık 256 MiB.
  Boyut sınırı daha erken silmeye yol açabilir; segment temizliği anlık kota değildir.
- Topic'ler Rental'daki `NewTopic` bean'leriyle açıkça oluşturulur. Broker'ın
  genel otomatik topic oluşturma ayarı kapalı kalır.
- Mesaj v1: eventId, eventType=`RentalCheckoutConfirmed`, schemaVersion=1,
  checkoutId, occurredAt. Token ve müşteri kişisel bilgisi içermez.
- Belge durumları: PENDING/READY/FAILED. GET yalnız okur, iş başlatmaz.
  `GET /api/rentals/checkouts/{checkoutId}/confirmation` CUSTOMER + kayıt sahipliği
  gerektirir; başka müşterinin kaydı 404 döner.
- Eski checkout'lar 25'lik gruplarla otomatik kuyruğa alınır. 15 dakikadan eski
  PENDING ve yayımlanmış işler yeniden gönderilebilir. READY belgenin outbox kaydı
  gönderimden 7 gün sonra 100'lük gruplarla temizlenir. Belgeler silinmez.
- Hazırlama 3 kez tekrar denenir (ilk denemeyle toplam 4). DB bağlantı/kilit
  gibi geçici altyapı hatalarında iş atlanmadan beklenir. Diğer kalıcı hatalar DLT'ye
  gönderilir ve ilgili iş FAILED yapılır. DLT gönderimi veya FAILED kaydı
  başarısızsa kaynak offset başarılı sayılmaz.

Proje kökünde kullanıcı çalıştırır:

```powershell
docker compose up -d --build gateway rental-service frontend
```

Rental Docker build'i ve Jenkins Rental aşaması normal Maven unit test keşfiyle
Confirmation ve kiralama kuralı testlerini çalıştırır; canlı DB/broker gerektirmeyen bu testler
PostgreSQL/Kafka entegrasyon testinin yerine geçmez. `tests/api/rental-confirmation.hurl`
mevcut checkout ve geçici runtime token'larıyla hedefli erişim/tekillik kontrolüdür;
kimlik bilgileri dosyaya eklenmez. Kullanıcı test istediğinde Gateway üzerinden çalıştırılır.

Öğrenme deneyinde consumer'ı geçici kapatmak için PowerShell oturumunda
`$env:RENTAL_CONFIRMATION_CONSUMER_ENABLED='false'` ayarlanıp yalnız Rental yeniden
oluşturulabilir: `docker compose up -d --no-deps --force-recreate rental-service`.
Deney sonunda değişken `Remove-Item Env:RENTAL_CONFIRMATION_CONSUMER_ENABLED` ile
kaldırılır ve aynı komutla consumer açılır. Bu, müşteri checkout HTTP akışını
çalışır bırakırken belge işlerinin Kafka'da biriktiğini göstermeyi sağlar.
Bu komutlar yalnız kontrollü deneyde kullanılır; 2026-11-16 kullanıcı onaylı
doğrulamasında consumer geçici kapatılıp normal ayarına geri döndürüldü.

DLT yeniden işleme: kök neden düzeltildikten sonra seçilen kaydın orijinal
key/value çifti ana topic'e **bir kez** yeniden gönderilir. Şema/key/eventId
doğrulaması korunur; READY kayıt atlanır, FAILED kayıt başarılı hazırlamada READY
olabilir. Bütün DLT'yi otomatik geri besleyen tüketici yoktur. Ana topic/broker
verisi sıfırlanırsa Rental topic'lerinin yeniden oluşturulması için Rental yeniden
başlatılır; bekleyen DB işleri yeniden yayınlanır. Eski checkout referansı olmayan
mesajlar rezervasyon veya belge oluşturamaz. Volume/veri sıfırlaması ayrıca onay gerektirir.

Bu yerel RF1/PLAINTEXT yapı yüksek erişilebilirlik veya production güvenliği sağlamaz.
Tarayıcı Kafka'ya bağlanmaz. PDF, indirme, yeni servis, ELK ve ödeme entegrasyonu bu kapsamda yoktur.

### Uygulama doğrulaması — 2026-11-16

Consumer kapalıyken iki mağaza/üç satırlı checkout HTTP 201 ve belge PENDING;
Kafka grubu lag=1 oldu. Consumer açılınca aynı belge READY oldu. Aynı ürünün farklı
tarih/adet satırları ve 2749 TL toplam Playwright ile belgede doğrulandı.
Yalnız broker kapalıyken başka checkout 201 döndü; outbox kaydı yayımlanmamış
olarak korundu ve Kafka açılınca UI polling ile READY oldu. Aynı eventId/key tekrar
gönderildiğinde belge kimliği, hazırlanma tarihi ve içerik değişmedi; çift kayıt yok.
Hedefli Hurl erişim/tekillik senaryosu 7/7 geçti. Son durumda consumer açık,
broker healthy, tüm partition lag=0 ve yayımlanmamış outbox=0.

Eski iki test kaydında günlük fiyat × gün ile kayıtlı toplam uyuşmadığından belge
FAILED ve olaylar DLT'de kaldı; veriler değiştirilmedi. DLT replay ve gerçek zamanlı
7 günlük temizlik deneyi bu sonuçlara dahil değildir. UI'daki rezervasyonlar tek
kartta birleştirildi; Kafka yönetim arayüzü eklenmedi.

pipeline {
    agent {
        label 'equipmenthub-ci'
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
    }

    stages {
        stage('Test and package Auth service') {
            steps {
                dir('services/auth-service') {
                    sh 'sh ./mvnw -B clean package'
                }
            }
            post {
                always {
                    junit testResults: 'services/auth-service/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Package Gateway') {
            steps {
                dir('services/gateway') {
                    sh 'sh ./mvnw -B -DskipTests clean package'
                }
            }
        }

        stage('Package Store service') {
            steps {
                dir('services/store-service') {
                    sh 'sh ./mvnw -B -DskipTests clean package'
                }
            }
        }

        stage('Package Catalog service') {
            steps {
                dir('services/catalog-service') {
                    sh 'sh ./mvnw -B -DskipTests clean package'
                }
            }
        }

        stage('Test and package Rental service') {
            steps {
                dir('services/rental-service') {
                    sh 'sh ./mvnw -B clean package'
                }
            }
            post {
                always {
                    junit testResults: 'services/rental-service/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Package Admin service') {
            steps {
                dir('services/admin-service') {
                    sh 'sh ./mvnw -B -DskipTests clean package'
                }
            }
        }

        stage('Check and build frontend') {
            steps {
                dir('frontend') {
                    nodejs(nodeJSInstallationName: 'equipmenthub-node24') {
                        sh 'npm ci --no-audit --no-fund'
                        sh 'npm run lint'
                        sh 'npm run build'
                    }
                }
            }
        }
    }
}

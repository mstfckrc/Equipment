pipeline {
    agent {
        label 'equipmenthub-ci'
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
    }

    stages {
        stage('Package Auth service') {
            steps {
                dir('services/auth-service') {
                    sh 'sh ./mvnw -B -DskipTests clean package'
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

        stage('Package Rental service') {
            steps {
                dir('services/rental-service') {
                    sh 'sh ./mvnw -B -DskipTests clean package'
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
    }
}

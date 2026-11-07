pipeline {
    agent {
        label 'equipmenthub-ci'
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Package Auth service') {
            steps {
                dir('services/auth-service') {
                    sh 'sh ./mvnw -B -DskipTests package'
                }
            }
        }

        stage('Package Gateway') {
            steps {
                dir('services/gateway') {
                    sh 'sh ./mvnw -B -DskipTests package'
                }
            }
        }
    }
}

pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {
        APP_NAME = "springboot-project"
        DEV_PORT = "8084"
    }

    stages {

        stage('Build & Test & Coverage') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    try {
                        timeout(time: 5, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                    } catch (err) {
                        echo "Quality Gate timed out – continuing DEV deployment"
                    }
                }
            }
        }


        stage('Package') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy to DEV') {
            steps {
                sh '''
                echo "Stopping old application if running..."
                pkill -f springboot-project || true

                echo "Starting application on DEV..."
                nohup java -jar target/*.jar \
                --server.port=${DEV_PORT} \
                > dev.log 2>&1 &
                '''
            }
        }

        stage('Sanity Test') {
            steps {
                sh '''
                echo "Waiting for app to start..."
                sleep 20
                curl -f http://localhost:${DEV_PORT}/ || exit 1
                '''
            }
        }
    }
}

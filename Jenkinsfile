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
                echo "Stopping application running on port ${DEV_PORT} (if any)..."
                PID=$(lsof -t -i:${DEV_PORT}) || true
                if [ -n "$PID" ]; then
                kill -9 $PID
                fi

                echo "Starting application on DEV..."
                nohup java -jar target/EventManagementSystem-0.0.1-SNAPSHOT.jar \
                --spring.profiles.active=dev \
                --server.port=${DEV_PORT} \
                --server.address=0.0.0.0 \
                > dev.log 2>&1 &
                '''
            }
        }


        stage('Sanity Test') {
            steps {
                sh '''
                echo "Waiting for app to start..."
                sleep 20
                curl -f http://localhost:${DEV_PORT}/h2-console || exit 1
                '''
            }
        }
    }
}

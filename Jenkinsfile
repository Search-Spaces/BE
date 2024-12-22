pipeline {
    agent any

    environment {
        IMAGE_NAME = "searchspaces"  // Docker 이미지 이름
        DOCKER_COMPOSE_FILE = "docker-compose.yml"  // docker-compose 파일 경로
    }

    stages {
        stage('Clone Repository') {
            steps {
                // GitHub에서 리포지토리 클론
                git branch: 'main',
                    url: 'https://github.com/Search-Spaces/BE.git'
            }
        }

        stage('Build Application') {
            steps {
                // Gradle Wrapper에 실행 권한 추가
                sh 'chmod +x ./gradlew'

                // Gradle을 사용하여 Spring Boot 애플리케이션 빌드 (dev 프로파일 활성화)
                sh './gradlew clean build -Pspring.profiles.active=dev -x test'
            }
        }

        stage('Build Docker Image') {
            steps {
                // Docker 이미지 빌드
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                // 기존 컨테이너 중지 및 Redis와 Spring Boot 컨테이너 실행
                sh '''
                docker-compose down || true
                docker-compose up -d
                '''
            }
        }
    }

    post {
        success {
            echo 'Deployment completed successfully!'
        }
        failure {
            echo 'Deployment failed. Check the logs for details.'
        }
    }
}

pipeline {
    agent any
    
    environment {
        NEXUS_URL = 'http://78.142.234.25:8081'
        NEXUS_REPO = 'maven-releases'
        TOMCAT_USER = 'admin'
        TOMCAT_PASS = '12345'
        TOMCAT_URL = 'http://78.142.234.25:8082/manager/text'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                }
            }
        }
        
        stage('Deploy to Nexus') {
            steps {
                sh '''
                mvn deploy:deploy-file \
                  -Durl=${NEXUS_URL}/repository/${NEXUS_REPO}/ \
                  -DrepositoryId=nexus \
                  -Dfile=target/${JOB_NAME}.war \
                  -DgroupId=com.example \
                  -DartifactId=${JOB_NAME} \
                  -Dversion=1.0 \
                  -Dpackaging=war
                '''
            }
        }
        
        stage('Deploy to Tomcat') {
            steps {
                sh '''
                curl -u ${TOMCAT_USER}:${TOMCAT_PASS} \
                  -F "deployWar=@target/${JOB_NAME}.war" \
                  "${TOMCAT_URL}/deploy?path=/${JOB_NAME}"
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Пайплайн успешно завершён! Приложение доступно по адресу: http://78.142.234.25:8082/${JOB_NAME}/hello'
        }
        failure {
            echo 'Пайплайн завершился с ошибкой!'
        }
    }
}

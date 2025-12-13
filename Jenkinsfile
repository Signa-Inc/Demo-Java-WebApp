pipeline {
    agent any
    
    environment {
        NEXUS_URL = 'http://localhost:8081'
        NEXUS_SNAPSHOT_REPO = 'maven-snapshots'  // Исправлено на snapshot
        TOMCAT_USER = 'admin'
        TOMCAT_PASS = '12345'
        TOMCAT_URL = 'http://localhost:8082/manager/text'
        JAVA_HOME = '/usr/lib/jvm/temurin-17-jdk-amd64'
        APP_CONTEXT = '/webapp-demo'
    }
    
    stages {
        // ... остальные стадии без изменений ...
        
        stage('Deploy to Nexus') {
            steps {
                sh '''
                /opt/maven/bin/mvn deploy:deploy-file \
                  -Durl=${NEXUS_URL}/repository/${NEXUS_SNAPSHOT_REPO}/ \
                  -DrepositoryId=nexus \
                  -Dfile=target/webapp-demo.war \
                  -DgroupId=com.example \
                  -DartifactId=webapp-demo \
                  -Dversion=1.0-SNAPSHOT \
                  -Dpackaging=war \
                  -Dusername=${TOMCAT_USER} \
                  -Dpassword=${TOMCAT_PASS}
                '''
            }
        }
        
        stage('Deploy to Tomcat') {
            steps {
                script {
                    def deployResponse = sh(
                        script: '''curl -s -w "\\n%{http_code}" -u ${TOMCAT_USER}:${TOMCAT_PASS} \
                        --upload-file target/webapp-demo.war \
                        "${TOMCAT_URL}/deploy?path=${APP_CONTEXT}&update=true"''',
                        returnStdout: true
                    ).trim()
                    
                    // Разделяем ответ и HTTP код
                    def responseLines = deployResponse.readLines()
                    def httpCode = responseLines[-1]
                    def responseMessage = responseLines[0..-2].join('\n')
                    
                    echo "Tomcat deployment response: ${responseMessage}"
                    echo "HTTP Status Code: ${httpCode}"
                    
                    // Проверяем успешность операции
                    if (httpCode != "200" || responseMessage.contains("FAIL")) {
                        error("Deployment to Tomcat failed: ${responseMessage}")
                    }
                }
            }
        }
        
        stage('Verify Application') {
            steps {
                script {
                    // Ждем пока приложение запустится
                    sleep time: 10, unit: 'SECONDS'
                    
                    // Проверяем доступность приложения
                    def appUrl = "http://localhost:8082${APP_CONTEXT}/hello"
                    def healthCheck = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' ${appUrl}",
                        returnStdout: true
                    ).trim()
                    
                    echo "Application health check result: ${healthCheck}"
                    
                    if (healthCheck != "200") {
                        // Попробуем получить логи для отладки
                        sh '''curl -u ${TOMCAT_USER}:${TOMCAT_PASS} \
                        "${TOMCAT_URL}/list"'''
                        error("Application failed to start. Health check returned: ${healthCheck}")
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo "Пайплайн успешно завершён! Приложение доступно по адресу: http://78.142.234.25:8082${APP_CONTEXT}/hello"
        }
        failure {
            echo "Пайплайн завершился с ошибкой! Требуется вмешательство."
            // Здесь можно добавить уведомление в Slack/Telegram/email
        }
    }
}
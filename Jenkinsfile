pipeline {
    agent any
    
    environment {
        NEXUS_URL = 'http://localhost:8081'
        NEXUS_SNAPSHOT_REPO = 'maven-snapshots'
        NEXUS_USER = 'admin'  // Правильные учетные данные Nexus
        NEXUS_PASS = '12345'  // Замени на реальный пароль Nexus
        TOMCAT_USER = 'admin'
        TOMCAT_PASS = '12345'
        TOMCAT_URL = 'http://localhost:8082/manager/text'
        JAVA_HOME = '/usr/lib/jvm/temurin-17-jdk-amd64'
        APP_CONTEXT = '/webapp-demo'
    }
    
    stages {
stage('Build') {
    steps {
        sh '''
            export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
            export PATH=$JAVA_HOME/bin:$PATH
            /opt/maven/bin/mvn compile
        '''
	echo "✅ Stage 'Build' is success complete!"
    }
}

stage('Test') {
    steps {
        sh '''
            export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
            export PATH=$JAVA_HOME/bin:$PATH
            /opt/maven/bin/mvn test
        '''
	    echo "✅ Stage 'Test' is success complete!"
    }
    post {
        always {
            archiveArtifacts artifacts: 'target/surefire-reports/**', allowEmptyArchive: true
        }
    }
}

stage('Package') {
    steps {
        sh '''
            export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
            export PATH=$JAVA_HOME/bin:$PATH
            /opt/maven/bin/mvn package -DskipTests
        '''
	    echo "✅ Stage 'Package' is success complete!"
    }
}
        
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
                  -Dusername=${NEXUS_USER} \
                  -Dpassword=${NEXUS_PASS}
                '''
	    echo "✅ Stage 'Deploy to Nexus' is success complete!"
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
                    
                    def responseLines = deployResponse.readLines()
                    def httpCode = responseLines[-1]
                    def responseMessage = responseLines[0..-2].join('\n')
                    
                    echo "Tomcat deployment response: ${responseMessage}"
                    echo "HTTP Status Code: ${httpCode}"
                    
                    if (httpCode != "200" || responseMessage.contains("FAIL")) {
                        error("Deployment to Tomcat failed: ${responseMessage}")
                    }
                }
	    echo "✅ Stage 'Deploy to Tomcat' is success complete!"
            }
        }
        
        stage('Verify Application') {
            steps {
                script {
                    // Увеличим время ожидания для полной инициализации приложения
                    echo "Waiting for application to fully initialize..."
                    sleep time: 30, unit: 'SECONDS'
                    
                    // Попробуем несколько раз проверить доступность приложения
                    def maxAttempts = 5
                    def attempt = 0
                    def healthCheckSuccess = false
                    
                    while (attempt < maxAttempts && !healthCheckSuccess) {
                        attempt++
                        echo "Health check attempt ${attempt} of ${maxAttempts}"
                        
                        def appUrl = "http://localhost:8082${APP_CONTEXT}/hello"
                        def healthCheck = sh(
                            script: "curl -s -o /dev/null -w '%{http_code}' ${appUrl}",
                            returnStdout: true
                        ).trim()
                        
                        echo "Application health check result: ${healthCheck}"
                        
                        if (healthCheck == "200") {
                            healthCheckSuccess = true
                            echo "Application is up and running!"
                        } else {
                            sleep time: 10, unit: 'SECONDS'
                        }
                    }
                    
                    if (!healthCheckSuccess) {
                        // Получить детальную информацию о приложении из Tomcat
                        echo "Getting detailed application status from Tomcat..."
                        sh '''curl -u ${TOMCAT_USER}:${TOMCAT_PASS} \
                        "${TOMCAT_URL}/list"'''
                        
                        // Попробовать получить логи приложения
                        echo "Getting application logs..."
                        sh '''curl -u ${TOMCAT_USER}:${TOMCAT_PASS} \
                        "${TOMCAT_URL}/getlog?path=${APP_CONTEXT}"'''
                        
                        error("Application failed to start after ${maxAttempts} attempts")
                    }
                }
	    echo "✅ Stage 'Verify Application' is success complete!"
            }
        }
    }
    
    post {
        success {
            echo "✅ Пайплайн успешно завершён! Приложение доступно по адресу: http://localhost:8082${APP_CONTEXT}/hello"
        }
        failure {
            echo "❌ Пайплайн завершился с ошибкой! Требуется вмешательство."
            sh 'echo "=== TOMCAT LOGS ==="; cat /opt/tomcat/logs/catalina.out || echo "Cannot access logs directly, trying Tomcat manager API"'
        }
    }
}
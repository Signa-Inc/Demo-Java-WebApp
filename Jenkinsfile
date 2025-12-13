pipeline {
    agent any
    
    environment {
        NEXUS_URL = 'http://78.142.234.25:8081'
        NEXUS_REPO = 'maven-releases'
        TOMCAT_USER = 'admin'
        TOMCAT_PASS = '12345'
        TOMCAT_URL = 'http://78.142.234.25:8082/manager/text'
        JAVA_HOME = '/usr/lib/jvm/temurin-17-jdk-amd64'
    }
    
    stages {
        stage('Debug') {
            steps {
                sh '''
                echo "=== DEBUG INFO ==="
                echo "PATH: $PATH"
                echo "USER: $(whoami)"
                echo "JAVA_HOME: $JAVA_HOME"
                echo "Java home directory contents:"
                ls -la $JAVA_HOME
                echo "Which java: $(which java)"
                java -version
                echo "Which mvn: $(which mvn || echo 'not found')"
                if [ -f /opt/maven/bin/mvn ]; then
                    echo "Maven exists at /opt/maven/bin/mvn"
                    /opt/maven/bin/mvn -v
                fi
                '''
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Fix BOM') {
            steps {
                sh '''
                echo "=== FIXING BOM IN JAVA FILES ==="
                # Проверяем наличие BOM до удаления
                echo "Before BOM removal:"
                find . -name "*.java" -exec file {} \\;
                find . -name "*.java" -exec hexdump -n 4 -C {} \\;
                
                # Надежный метод удаления BOM
                for file in $(find . -name "*.java"); do
                    echo "Processing $file"
                    # Проверяем, есть ли BOM
                    if head -c3 "$file" | grep -q $'\\xEF\\xBB\\xBF'; then
                        echo "BOM detected in $file - removing"
                        tail -c +4 "$file" > "$file.tmp"
                        mv "$file.tmp" "$file"
                        echo "BOM removed from $file"
                    else
                        echo "No BOM detected in $file"
                    fi
                done
                
                # Проверяем результат
                echo "After BOM removal:"
                find . -name "*.java" -exec file {} \\;
                find . -name "*.java" -exec hexdump -n 4 -C {} \\;
                '''
            }
        }
        
        stage('Build') {
            steps {
                sh '/opt/maven/bin/mvn clean package -DskipTests'
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
/opt/maven/bin/mvn deploy:deploy-file \
  -Durl=http://78.142.234.25:8081/repository/maven-releases/ \
  -DrepositoryId=nexus \
  -Dfile=target/webapp-demo.war \
  -DgroupId=com.example \
  -DartifactId=webapp-demo \
  -Dversion=1.0 \
  -Dpackaging=war \
  -Dusername=admin \
  -Dpassword=12345
        '''
    }
}
        
        stage('Deploy to Tomcat') {
            steps {
                sh '''
                curl -u ${TOMCAT_USER}:${TOMCAT_PASS} \
                  -F "deployWar=@target/webapp-demo.war" \
                  "${TOMCAT_URL}/deploy?path=/webapp-demo"
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Пайплайн успешно завершён! Приложение доступно по адресу: http://78.142.234.25:8082/webapp-demo/hello'
        }
        failure {
            echo 'Пайплайн завершился с ошибкой!'
        }
    }
}
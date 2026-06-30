pipeline {
    agent any
    
    environment {
        GIT_URL = 'https://github.com/your-org/part-time-platform.git'
        DOCKER_REGISTRY = 'registry.example.com'
        PROJECT_NAME = 'parttime'
        BUILD_NUMBER = "${BUILD_NUMBER}"
    }
    
    tools {
        maven 'Maven 3.8.6'
        nodejs 'Node 20.x'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git url: "${GIT_URL}", branch: 'main', credentialsId: 'git-credentials'
            }
            post {
                success {
                    echo '代码拉取成功'
                }
                failure {
                    echo '代码拉取失败'
                    emailext body: '代码拉取失败，请检查Git仓库连接', subject: '[CI失败] 代码拉取失败', to: 'devops@example.com'
                }
            }
        }
        
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests -q'
                }
            }
            post {
                success {
                    echo '后端构建成功'
                }
                failure {
                    echo '后端构建失败'
                    emailext body: '后端构建失败，请检查编译错误', subject: '[CI失败] 后端构建失败', to: 'devops@example.com'
                }
            }
        }
        
        stage('Build Frontend') {
            parallel {
                stage('PC Web') {
                    steps {
                        dir('pc-web') {
                            sh 'npm ci --silent'
                            sh 'npm run build --silent'
                        }
                    }
                    post {
                        failure {
                            emailext body: 'PC端构建失败', subject: '[CI失败] PC端构建失败', to: 'devops@example.com'
                        }
                    }
                }
                
                stage('H5 Web') {
                    steps {
                        dir('h5-web') {
                            sh 'npm ci --silent'
                            sh 'npm run build --silent'
                        }
                    }
                    post {
                        failure {
                            emailext body: 'H5端构建失败', subject: '[CI失败] H5端构建失败', to: 'devops@example.com'
                        }
                    }
                }
                
                stage('MiniApp') {
                    steps {
                        dir('miniapp') {
                            sh 'npm ci --silent'
                            sh 'npm run build:h5 --silent'
                            sh 'npm run build:mp-weixin --silent'
                        }
                    }
                    post {
                        failure {
                            emailext body: '小程序构建失败', subject: '[CI失败] 小程序构建失败', to: 'devops@example.com'
                        }
                    }
                }
            }
            post {
                success {
                    echo '前端构建成功'
                }
            }
        }
        
        stage('Test') {
            steps {
                dir('backend') {
                    sh 'mvn test -q'
                }
            }
            post {
                success {
                    echo '测试通过'
                    junit 'backend/**/target/surefire-reports/*.xml'
                }
                failure {
                    echo '测试失败'
                    junit 'backend/**/target/surefire-reports/*.xml'
                    emailext body: '单元测试失败，请检查测试用例', subject: '[CI失败] 单元测试失败', to: 'devops@example.com'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    def services = [
                        'gateway', 'user-service', 'job-service', 'match-service',
                        'im-service', 'task-service', 'payment-service', 'complaint-service',
                        'notification-service', 'admin-service', 'data-service'
                    ]
                    
                    services.each { service ->
                        sh "docker build -f backend/Dockerfile.${service} -t ${DOCKER_REGISTRY}/${PROJECT_NAME}-${service}:${BUILD_NUMBER} backend"
                        sh "docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}-${service}:${BUILD_NUMBER}"
                    }
                }
            }
            post {
                success {
                    echo 'Docker镜像构建并推送成功'
                }
                failure {
                    echo 'Docker镜像构建或推送失败'
                    emailext body: 'Docker镜像构建或推送失败', subject: '[CI失败] Docker构建失败', to: 'devops@example.com'
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sh 'sed -i "s/IMAGE_TAG/${BUILD_NUMBER}/g" docker-compose.yml'
                sh 'docker-compose up -d'
            }
            post {
                success {
                    echo '部署成功'
                    emailext body: '项目部署成功', subject: '[CI成功] 项目部署完成', to: 'devops@example.com'
                }
                failure {
                    echo '部署失败'
                    emailext body: '部署失败，请检查服务器状态', subject: '[CI失败] 部署失败', to: 'devops@example.com'
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'CI/CD流水线执行成功'
        }
        failure {
            echo 'CI/CD流水线执行失败'
        }
    }
}
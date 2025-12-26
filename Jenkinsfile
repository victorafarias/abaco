pipeline {
  agent {
    kubernetes {
      label 'docker'
      defaultContainer 'maven'
    }
  }

  environment {
    REGISTRY = 'basis-registry.basis.com.br'
    REGISTRY_CREDENTIAL = 'jenkins-nexus-user'
    SISTEMA = 'abaco'
    FIXED_TAG_BRANCHES = '' 
  }

  stages {
    stage('Setup') {
      steps {
        script {
          env.SERVICES = 'frontend,backend' 
          env.SAFEBRANCH = BRANCH_NAME.replaceAll('/', '-')
          echo "Iniciando build do sistema: ${env.SISTEMA} na branch: ${env.SAFEBRANCH}"
        }
      }
    }

    stage('Maven Build Backend') {
      steps {
        container('maven') {
          dir('backend') {
            sh 'mvn clean package -DskipTests -s /home/jenkins/.m2/settings.xml'
          }
        }
      }
    }

    stage('Docker Build and Deploy') {
      steps {
        script {
          def services = env.SERVICES.split(',').collect { it.trim() }
          def isFixedTagBranch = (BRANCH_NAME == env.FIXED_TAG_BRANCHES)

          //socket local
          withEnv(['DOCKER_HOST=']) { 
            
            container('docker') {
              withCredentials([usernamePassword(credentialsId: REGISTRY_CREDENTIAL, usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
                sh 'echo $REG_PASS | docker login -u $REG_USER --password-stdin $REGISTRY'
              }

              services.each { service ->
                def imageTag = "${REGISTRY}/${env.SISTEMA}/${service}:${env.SAFEBRANCH}-${BUILD_NUMBER}"
                def imageTagLatest = "${REGISTRY}/${env.SISTEMA}/${service}:${env.SAFEBRANCH}"

                stage("Build Docker ${service}") {
                  echo "Building ${service}..."
                  // Caminho ajustado para ./${service}
                  sh "docker build -t ${imageTag} ./${service}"
                  sh "docker push ${imageTag}"

                  if (isFixedTagBranch) {
                    sh "docker tag ${imageTag} ${imageTagLatest}"
                    sh "docker push ${imageTagLatest}"
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
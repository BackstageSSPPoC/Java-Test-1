pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        APP_NAME = " Java-Test-1".toLowerCase().trim()
        DOCKER_IMAGE = "chaitanyapandeygspann/${APP_NAME}"
        DOCKER_TAG = "1.0.${BUILD_NUMBER}"
        IMAGE_TAG = "${DOCKER_IMAGE}:${DOCKER_TAG}"
        GITOPS_REPO = "https://github.com/BackstageSSPPoC/k8s-manifests.git"
    }

    stages {

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Decide Pipeline Flow') {
            steps {
                script {
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "GIT_BRANCH: ${env.GIT_BRANCH}"

                    if (env.BRANCH_NAME == "main" || env.BRANCH_NAME.endsWith("/main") || env.GIT_BRANCH?.endsWith("main")) {
                        echo "Main branch detected → CI + CD"
                        env.RUN_MODE = "cd"
                    } else {
                        echo "Non-main branch → CI only"
                        env.RUN_MODE = "ci"
                    }
                }
            }
        }

// ================= CI STAGES =================

        stage('Build & Test') {
            steps {
                sh 'mvn clean verify -B'
                // -B = batch mode (no interactive prompts, clean Jenkins output)
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                    mvn sonar:sonar \
                      -Dsonar.projectKey=${APP_NAME} \
                      -Dsonar.sources=. \
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

// ================= CD STAGES =================

        stage('Build Docker Image') {
            when {
                expression { env.RUN_MODE == "cd" }
            }
            steps {
                sh 'docker build -t ${IMAGE_TAG} .'
            }
        }

        stage('Login to Docker Hub') {
            when {
                expression { env.RUN_MODE == "cd" }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                }
            }
        }

        stage('Push Docker Image') {
            when {
                expression { env.RUN_MODE == "cd" }
            }
            steps {
                sh 'docker push ${IMAGE_TAG}'
            }
        }

        stage('Update GitOps Repo') {
            when {
                expression { env.RUN_MODE == "cd" }
            }
            steps {
                withCredentials([string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                    rm -rf k8s-manifests

                    git clone --depth 1 https://${GITHUB_TOKEN}@github.com/BackstageSSPPoC/k8s-manifests.git
                    cd k8s-manifests

                    mkdir -p apps/${APP_NAME}
                    cp -r ../manifest-templates/* apps/${APP_NAME}/ || true

                    sed -i "s|\\${APP_NAME}|${APP_NAME}|g" apps/${APP_NAME}/*.yaml || true
                    sed -i "s|\\${DOCKER_IMAGE}|${IMAGE_TAG}|g" apps/${APP_NAME}/deployment.yaml || true

                    git config user.email "jenkins@local"
                    git config user.name "jenkins"
                    git add .
                    git commit -m "Deploy ${APP_NAME} build ${BUILD_NUMBER}" || echo "No changes"
                    git push origin main
                    '''
                }
            }
        }
    }

    post {
        always {
            sh "docker logout || true"
            sh "docker image prune -f || true"
        }
    }
}

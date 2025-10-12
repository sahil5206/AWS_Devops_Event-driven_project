pipeline {
    agent any
    stages {
        stage('Build Website') {
            steps {
                sh 'docker build -t gcr.io/${Project_ID}/website:latest ./website'
            }
        }
        stage('Build Producer'){
            steps {
                sh 'docker build -t gcr.io/${Project_ID}/kafka-producer:latest ./kafka'
            }
        }
        stage ('Push to GCR') {
            steps {
                sh 'gcloud auth donfigure-docker'
                sh 'docker push gcr.io/${Project_ID}/website:latest'
                sh 'docker push gcr.io/${Project_ID}/kafka-producer:latest'
            }
        }
        stage('Deploy to GKE') {
            steps {
                sh 'kubectl apply -f ./website/k8s-deployment.yaml'
                sh 'kubectl apply -f ./kafka/k8s-deployment.yaml'
            }
        }
    }
}
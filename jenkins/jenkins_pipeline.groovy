pipeline {
    agent any

    environment {
        AWS_REGION     = 'us-east-1'        // override via Jenkins env/params
        AWS_ACCOUNT_ID = '000000000000'     // set to your AWS account ID
        EKS_CLUSTER    = 'event-driven-eks' // existing or planned cluster name
    }

    stages {
        stage('Validate AWS context') {
            steps {
                sh '''
                if [ -z "$AWS_ACCOUNT_ID" ] || [ "$AWS_ACCOUNT_ID" = "000000000000" ]; then
                  echo "AWS_ACCOUNT_ID must be supplied before running the pipeline."
                  exit 1
                fi
                aws sts get-caller-identity --output text
                '''
            }
        }

        stage('Login to ECR') {
            steps {
                sh '''
                ECR_REGISTRY=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

                aws ecr describe-repositories --repository-names website || \
                  aws ecr create-repository --repository-name website --image-scanning-configuration scanOnPush=true
                aws ecr describe-repositories --repository-names kafka || \
                  aws ecr create-repository --repository-name kafka --image-scanning-configuration scanOnPush=true

                aws ecr get-login-password --region ${AWS_REGION} | \
                  docker login --username AWS --password-stdin ${ECR_REGISTRY}
                '''
            }
        }

        stage('Build Docker images') {
            steps {
                sh '''
                ECR_REGISTRY=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                docker build -t ${ECR_REGISTRY}/website:latest ./website
                docker build -t ${ECR_REGISTRY}/kafka:latest ./kafka
                '''
            }
        }

        stage('Push to ECR') {
            steps {
                sh '''
                ECR_REGISTRY=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                docker push ${ECR_REGISTRY}/website:latest
                docker push ${ECR_REGISTRY}/kafka:latest
                '''
            }
        }

        stage('Deploy to EKS') {
            steps {
                sh '''
                aws eks update-kubeconfig --name ${EKS_CLUSTER} --region ${AWS_REGION}
                sed -e "s|<AWS_ACCOUNT_ID>|${AWS_ACCOUNT_ID}|g" -e "s|<AWS_REGION>|${AWS_REGION}|g" website/k8s-deployment.yaml | kubectl apply -f -
                sed -e "s|<AWS_ACCOUNT_ID>|${AWS_ACCOUNT_ID}|g" -e "s|<AWS_REGION>|${AWS_REGION}|g" kafka/k8s-deployment.yaml | kubectl apply -f -
                kubectl apply -f ./monitoring/k8s-deployment.yaml
                '''
            }
        }
    }
}
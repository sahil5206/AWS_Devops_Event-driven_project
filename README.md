## Event-Driven Observability Stack (AWS Edition)

This project stands up an event-driven demo workload on AWS: a web UI emits user events, a Java-based Kafka producer publishes them, a consumer service exposes recent activity, and Prometheus/Grafana monitor the pipeline. Infrastructure is provisioned with Terraform (VPC, subnet, security group), CI/CD runs through Jenkins, and workloads can target EKS, kind, or any Kubernetes cluster.

### Components
- `infra/`: Terraform configuration for an AWS VPC, public subnet, routing, and security group that expose the required ports.
- `jenkins/`: Dockerized Jenkins master plus pipeline script that builds images, creates the ECR repositories if needed, pushes the Kafka/website artifacts, and deploys manifests to your cluster via `aws eks update-kubeconfig`.
- `kafka/`: Java producer/consumer apps with Docker/Kubernetes manifests. Producers expose `/send`, consumers expose `/events`.
- `website/`: Static front-end that triggers synthetic events and visualizes recent metrics with Chart.js.
- `monitoring/`: Prometheus + Grafana docker-compose and Kubernetes manifests with datasource/dashboard provisioning.

### AWS prerequisites
1. AWS account with billing enabled and IAM credentials (Access key or profile) that can manage VPC, ECR, and EKS.
2. CLI tooling on your workstation/Jenkins agent:
   - `awscli v2`
   - `kubectl`
   - `docker` / `docker compose`
   - `terraform >= 1.5`

### Quick start
```bash
# 1. Configure AWS CLI credentials/profile
aws configure --profile event-driven

# 2. Provision networking
cd infra
terraform init
terraform apply -var="region=us-east-1" -var="aws_profile=event-driven"

# 3. Build and push containers (via Jenkins pipeline or manually)
docker build -t ${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/website:latest ./website
docker build -t ${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/kafka:latest ./kafka

# 4. Update kubeconfig for your EKS (or local) cluster and apply manifests
aws eks update-kubeconfig --name <cluster-name> --region <aws-region>
kubectl apply -f website/k8s-deployment.yaml
kubectl apply -f kafka/k8s-deployment.yaml
kubectl apply -f monitoring/k8s-deployment.yaml
```

> **Note:** Replace `ACCOUNT_ID`, `<cluster-name>`, and `<aws-region>` with your environment’s values. The Kubernetes manifests reference AWS ECR images using the pattern `${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/<repo>:latest`.

### Local-only option
If you can’t access AWS resources yet, run the entire stack locally:
```bash
docker compose -f kafka/docker-compose.yml up -d
docker compose -f monitoring/docker-compose.yml up -d
docker build -t website-ui ./website && docker run -p 3000:80 website-ui
```
Update `website/app.js` to point `PRODUCER_API` at `http://localhost:8080/send` when running producer locally.


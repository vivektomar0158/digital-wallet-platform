# Digital Wallet & P2P Payment Platform

<img width="450" height="500" alt="Screenshot 2026-02-05 133645" src="https://github.com/user-attachments/assets/b4d0f226-4f16-4f78-89bf-77a4d38305dc" />
<img width="450" height="500" alt="Screenshot 2026-02-05 133656" src="https://github.com/user-attachments/assets/86b8be02-e4b4-41b2-bca0-d0c293beda94" />

## Overview

A scalable **Digital Wallet and Peer-to-Peer (P2P) Payment Platform** built using modern cloud-native technologies. The platform allows users to manage wallet balances, perform peer-to-peer transactions, and integrate with cloud services for storage, messaging, caching, and monitoring.

This project demonstrates production-grade backend architecture using **Spring Boot microservices**, **Docker**, and **Kubernetes**, with AWS service integrations.

---

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- JWT Authentication
- Maven

### Database & Storage
- PostgreSQL (AWS RDS)
- Redis (AWS ElastiCache)
- AWS S3 (File Storage)

### Messaging & Async Processing
- AWS SQS

### Containerization & Orchestration
- Docker
- Kubernetes (AWS EKS / Local Kubernetes)

### CI/CD & Monitoring
- GitHub Actions
- AWS ECR
- AWS CloudWatch
- Prometheus
- Grafana

---

## Features

- User authentication & authorization using JWT
- Wallet management
- P2P money transfer
- Transaction tracking
- Redis caching for performance
- Asynchronous processing using SQS
- Cloud storage integration using S3
- Kubernetes-based container orchestration
- Swagger UI for API testing
- Production-ready monitoring stack

---

## Project Structure

digital-wallet-platform
│
├── src/ # Spring Boot source code
├── k8s/ # Kubernetes deployment manifests
├── Dockerfile # Docker image configuration
├── docker-compose.yml # Local container orchestration
├── pom.xml # Maven build configuration
└── README.md


---

## Prerequisites

Make sure you have the following installed:

- Java 17
- Maven
- Docker Desktop
- Kubernetes enabled in Docker Desktop
- kubectl CLI
- Git

---

## Clone Repository

```bash
git clone https://github.com/vivektomar0158/digital-wallet-platform.git
cd digital-wallet-platform
Build Docker Image
docker build -t digital-wallet-platform:latest .

Deploy Locally Using Kubernetes
Step 1: Apply Configuration Files
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

Step 2: Deploy Infrastructure Services
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/localstack.yaml

Step 3: Deploy Application
kubectl apply -f k8s/app.yaml

Verify Deployment
kubectl get pods
kubectl get svc
kubectl get nodes -o wide

Access Swagger API Documentation
http://localhost:30080/swagger-ui.html


If NodePort is not accessible, use port forwarding:

kubectl port-forward svc/digital-wallet-service 8080:8080


Then open:

http://localhost:8080/swagger-ui.html

Environment Variables

The application uses Kubernetes ConfigMaps and Secrets to configure:

Database credentials

Redis connection

AWS credentials

JWT secrets

Application configuration

Local Development Using Docker Compose (Optional)
docker-compose up -d

Monitoring & Logging

Prometheus collects metrics

Grafana visualizes metrics

AWS CloudWatch handles logging in production

Scaling Application
kubectl scale deployment digital-wallet --replicas=2

Stop Deployment
Stop Only Application
kubectl delete -f k8s/app.yaml

Stop Entire Stack
kubectl delete -f k8s/app.yaml
kubectl delete -f k8s/postgres.yaml
kubectl delete -f k8s/redis.yaml
kubectl delete -f k8s/localstack.yaml
kubectl delete -f k8s/configmap.yaml
kubectl delete -f k8s/secret.yaml

Remove Docker Image
docker rmi digital-wallet-platform:latest

Production Deployment Flow

Build Docker image

Push image to AWS ECR

Deploy image to AWS EKS

Use AWS RDS and ElastiCache

Configure Ingress & Load Balancer

Monitor using Prometheus + Grafana + CloudWatch

Troubleshooting
Pods Not Starting
kubectl logs <pod-name>
kubectl describe pod <pod-name>

Image Pull Errors

Ensure image exists in registry or loaded locally.

Service Not Accessible

Check NodePort and pod labels.

Future Improvements

Implement persistent storage for database

Add API rate limiting

Add distributed tracing

Implement payment gateway integrations

Add event-driven architecture using Kafka

Add Helm charts for simplified deployment

Author

Vivek Tomar
Email: vivektomar0158@gmail.com
GitHub: https://github.com/vivektomar0158

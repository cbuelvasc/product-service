# Kubernetes deployment (Minikube)

Manifests to deploy **product-service** and **Redis** on a local Minikube cluster.

## Requirements

- [Minikube](https://minikube.sigs.k8s.io/docs/start/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- Docker (or the driver you use with Minikube)

## 1. Start Minikube

```bash
minikube start
```

Optional: use the host Docker daemon so the image is built inside the cluster:

```bash
eval $(minikube docker-env)
```

## 2. Build the product-service image

The application must be packaged as a Docker image. From the project root:

```bash
# Using Minikube's Docker (recommended so the image is available in the cluster)
eval $(minikube docker-env)
docker build -t product-service:1.0.0 .
```

If you don't use `minikube docker-env`, you can load the image into Minikube afterward:

```bash
minikube image load product-service:1.0.0
```

**Note:** The Deployment uses `imagePullPolicy: Never` so Kubernetes does not try to pull the image from a registry and uses the one you built or loaded in Minikube. If you later use a registry (e.g. GHCR), change to `IfNotPresent` or `Always` and use the full image URL.

## 3. Deploy the manifests

Deploy Redis first, then the application (the service depends on Redis):

```bash
kubectl apply -f k8s/redis/
kubectl apply -f k8s/product-service/
```

Or apply everything at once:

```bash
kubectl apply -f k8s/redis/ -f k8s/product-service/
```

## 4. Verify the deployment

```bash
kubectl get pods
kubectl get svc
```

When the pods are `Running` and `Ready`, the application is available.

## 5. Access the application

**Option A – Port-forward (recommended for development)**

```bash
kubectl port-forward svc/product-service 8080:80
```

Keep the command running; the API is available at `http://localhost:8080/api/product-service` (Swagger, health, etc.). Same URL as when running locally without Kubernetes.

**Option B – NodePort (port 30080)**

```bash
minikube service product-service --url
```

Then open in your browser the URL it prints (e.g. `http://192.168.49.2:30080`).

- API base: `http://<minikube-ip>:30080/api/product-service`
- Swagger UI: `http://<minikube-ip>:30080/api/product-service/swagger-ui.html`
- Health: `http://<minikube-ip>:30080/api/product-service/admin/health`

**Option C – Tunnel (simulated LoadBalancer)**

If you change the product-service Service to `type: LoadBalancer`:

```bash
minikube tunnel
```

Then use the external IP that Minikube assigns.

## 6. Logs and troubleshooting

```bash
# product-service logs
kubectl logs -f deployment/product-service-deployment

# Redis logs
kubectl logs -f deployment/redis-deployment

# Pod description (events, status)
kubectl describe pod -l app=product-service
```

## 7. Remove the deployment

```bash
kubectl delete -f k8s/product-service/ -f k8s/redis/
```

## Structure

```
k8s/
├── README.md              # This file
├── microservices/         # Reference example (ticket-service)
├── redis/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── README.md          # Deploy, verify, logs
└── product-service/
    ├── configmap.yaml     # application.yaml mounted at /config
    ├── deployment.yaml
    ├── service.yaml
    └── README.md          # Detailed deploy, access, logs, troubleshooting
```

- **Redis**: Deployment `redis-deployment` with 1 replica, ClusterIP Service `redis` on port 6379. See [k8s/redis/README.md](redis/README.md).
- **product-service**: ConfigMap for app config, Deployment with startup/liveness/readiness probes, env vars `REDIS_HOST=redis` and `REDIS_PORT=6379`, NodePort Service on 30080. See [k8s/product-service/README.md](product-service/README.md) for details.

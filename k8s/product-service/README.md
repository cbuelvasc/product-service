# Product Service – Kubernetes deployment

This directory contains the Kubernetes manifests to deploy **product-service** (and assumes Redis is deployed, e.g. from `../redis/`).

## Files

| File | Description |
|------|-------------|
| **configmap.yaml** | Application configuration (full `application.yaml` mounted in the pod) |
| **deployment.yaml** | Deployment with ConfigMap mount, health checks (startup/liveness/readiness) |
| **service.yaml** | NodePort Service for external access |

## Prerequisite: Docker image

Ensure the Docker image is available before deploying.

**Option A: Minikube (local image)**

```bash
eval $(minikube docker-env)
docker build -t product-service:1.0.0 .
```

**Option B: Image in a registry**

```bash
docker build -t product-service:1.0.0 .
# push to your registry, then set imagePullPolicy: IfNotPresent or Always in deployment.yaml
```

## Deploy

Deploy Redis first (from repo root):

```bash
kubectl apply -f k8s/redis/
```

Then deploy product-service:

```bash
# From repo root – apply all product-service manifests
kubectl apply -f k8s/product-service/
```

Or apply in order:

```bash
kubectl apply -f k8s/product-service/configmap.yaml
kubectl apply -f k8s/product-service/deployment.yaml
kubectl apply -f k8s/product-service/service.yaml
```

## Configuration

| Item | Value |
|------|--------|
| App name | product-service |
| Image | product-service:1.0.0 |
| Replicas | 1 |
| NodePort | 30080 |
| Context path | /api/product-service |

Config is loaded from the ConfigMap at `/config/application.yaml`; Redis is configured via env vars `REDIS_HOST` and `REDIS_PORT` (default: `redis:6379`).

## Access

**Option 1: Port-forward (recommended for local)**

```bash
kubectl port-forward svc/product-service 8080:80
```

- API: http://localhost:8080/api/product-service  
- Compare: http://localhost:8080/api/product-service/products/compare?ids=1,2  
- Swagger: http://localhost:8080/api/product-service/swagger-ui.html  
- Health: http://localhost:8080/api/product-service/admin/health  

**Option 2: Minikube NodePort**

```bash
minikube service product-service --url
# Then use the printed URL, e.g. http://<ip>:30080/api/product-service
```

**Option 3: Direct NodePort**

```bash
curl "http://$(minikube ip):30080/api/product-service/products/compare?ids=1,2"
```

## Verify

```bash
kubectl get deployment product-service-deployment
kubectl get pods -l app=product-service
kubectl get svc product-service
kubectl get configmap product-service-config
```

## Logs

```bash
# All product-service pods
kubectl logs -l app=product-service -f --tail=50

# One pod
kubectl logs -l app=product-service -f
kubectl logs <pod-name> -f --tail=100
kubectl logs <pod-name> --previous
```

## Update image

```bash
kubectl set image deployment/product-service-deployment \
  product-service=product-service:1.0.1

kubectl rollout status deployment/product-service-deployment
```

## Troubleshooting

**ImagePullBackOff**

- For Minikube, build inside Minikube’s Docker: `eval $(minikube docker-env)` then `docker build -t product-service:1.0.0 .`
- Deployment uses `imagePullPolicy: Never` for local images; change to `IfNotPresent` or `Always` when using a registry.

**Pods not starting**

```bash
kubectl describe pod -l app=product-service
kubectl logs -l app=product-service
kubectl exec -it <pod-name> -- cat /config/application.yaml
kubectl get events --sort-by='.lastTimestamp'
```

**Service not reachable**

```bash
kubectl get endpoints product-service
kubectl describe svc product-service
```

Ensure Redis is running and product-service can resolve `redis` (same namespace or correct DNS).

## Delete

```bash
kubectl delete -f k8s/product-service/
```

# Redis – Kubernetes deployment

Manifests to deploy **Redis** for use by product-service (and other services in the same namespace).

## Files

| File | Description |
|------|-------------|
| **deployment.yaml** | Deployment `redis-deployment`, 1 replica, liveness/readiness probes |
| **service.yaml** | ClusterIP Service `redis` on port 6379 (used by product-service as `REDIS_HOST=redis`) |

## Deploy

From the repo root:

```bash
kubectl apply -f k8s/redis/
```

Deploy Redis before product-service so the app can connect at startup.

## Verify

```bash
kubectl get deployment redis-deployment
kubectl get pods -l app=redis
kubectl get svc redis
```

## Logs

```bash
kubectl logs -f deployment/redis-deployment
kubectl logs -l app=redis -f --tail=50
```

## Delete

```bash
kubectl delete -f k8s/redis/
```

# Redis – Kubernetes deployment

Manifests to deploy **Redis** for use by product-service (and other services in the same namespace). All resources use the label `managed-by: kubernetes`. Deploy Redis before [product-service](../product-service/README.md).

## Files

| File | Description |
|------|-------------|
| **deployment.yaml** | Deployment `redis-deployment`: 1 replica, image `redis:7.0-alpine`, liveness/readiness probes with `failureThreshold: 3` |
| **service.yaml** | Service `redis`: ClusterIP on port 6379 (product-service uses `REDIS_HOST=redis`, `REDIS_PORT=6379`) |

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

From repo root:

```bash
kubectl delete -f k8s/redis/
```

Remove Redis only after stopping product-service (or any other consumer) to avoid connection errors.

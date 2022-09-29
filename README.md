
# K8s commands
```
kubectl create -f kafka-example.yaml
kubectl logs -f deploy/kafka-example
kubectl delete flinkdeployment/kafka-example
```

# UI
```
kubectl port-forward svc/kafka-example-rest 8081
```
# Build
```
sbt clean assembly
docker build -t flink-k8s .
```
# Data for Producer
```
minikube mount /tmp/data:/tmp/data
```

# K8s commands
```
kubectl apply -f kafka-k8s.yml
kubectl create -f kafka-consumer.yaml
kubectl logs -f deploy/kafka-consumer
kubectl delete flinkdeployment/kafka-consumer
```

# KafkaCat
```
kafkacat -b kafka-svc -L
kafkacat -b kafka-svc -t logs-sink -P
kafkacat -b kafka-svc -t logs-source -P -l /tmp/data/access.log
kafkacat -b kafka-svc -t logs-source -C -o earliest -c 20
kafkacat -b kafka-svc -t logs-sink -C -o earliest -c 20
```

# UI
```
kubectl port-forward svc/kafka-consumer-rest 8081
```
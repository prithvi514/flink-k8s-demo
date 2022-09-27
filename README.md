
# K8s commands
```
kubectl create -f wordcount-job.yaml
kubectl logs -f deploy/basic-example
kubectl delete flinkdeployment/basic-example
```

# Errors
```
Caused by: org.apache.flink.api.common.InvalidProgramException: Job was submitted in detached mode. Results of job execution, such as accumulators, runtime, etc. are not available. Please make sure your program doesn't call an eager execution function [collect, print, printToErr, count].
```
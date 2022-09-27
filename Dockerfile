FROM flink:1.14
COPY target/scala-2.11/flink-k8s-demo-assembly-0.1-SNAPSHOT.jar /opt/flink/examples/streaming/
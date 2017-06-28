## Simple Demo of Querying Remote JDG Server

### Running the demo

1. Start a JDG server in domain mode on your local machine, by default one of the JDG nodes should run at 127.0.0.1:11222
2. Run the project with the following command from the root folder :

```sh
# To run without skipping inserts and protobuf publishing
mvn clean compile exec:java

# To run with skipping inserts and protobuf publishing 
mvn -DskipInserts -DskipProtobuf exec:java
```

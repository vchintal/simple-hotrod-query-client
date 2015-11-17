## Simple Demo of Querying Remote JDG Server

### Running the demo

1. Start a JDG server on your local machine, by default it should run at 127.0.0.1:11222
2. Run `mvn clean package` in the root folder of the project
3. Run the project with the following command from the root folder :

```sh
java -cp target/simple-hotrod-query-client-jar-with-dependencies.jar org.everythingjboss.jdg.SimpleHotRodQueryClient
```

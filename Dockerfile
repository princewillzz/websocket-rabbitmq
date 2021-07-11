FROM openjdk:11-jre-slim
EXPOSE 8080
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT [ "java", "-jar", "/app.jar" ]


# docker run --rm --hostname my-rabbit --net untanglechat --name some-rabbit -p 15672:15672 -p 5671:5672 rabbitmq:3-management
# docker run --rm --net untanglechat -p 8080:8080 chatapp
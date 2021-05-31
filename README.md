# websocket-rabbitmq

## Step to run
### start the rabbitmq server 
##### command: docker run --hostname my-rabbit --name some-rabbit -p 15672:15672 -p 5671:5672 rabbitmq:3-management
now you can access to rabbitmq dashboard on the URL: http://localhost:15672/  default username and password is guest

### start the spring app
##### command: ./mvnw spring-boot:run

Please check your application.properties file for any setting changes

Now visit http://localhost:8080 to use websockets
~ send message directly to a queue by sending a post request to url http://localhost:8080/publish/{queueName} with message in the body of the request
where queuename is the name of queue, It will be created if it does not exist 




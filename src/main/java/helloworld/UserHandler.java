package helloworld;

import com.amazon.dax.client.dynamodbv2.AmazonDaxClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User CRUD lambda function handlers
 */
public class UserHandler  {

    private DynamoDBMapper initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        return new DynamoDBMapper(client);
    }

    private DynamoDBMapper initDax(){
        AmazonDaxClientBuilder daxClientBuilder = AmazonDaxClientBuilder.standard();
        //todo
        daxClientBuilder.withRegion("us-west-2").withEndpointConfiguration("");
        AmazonDynamoDB client = daxClientBuilder.build();
        return new DynamoDBMapper(client);
    }
    public APIGatewayProxyResponseEvent create(APIGatewayProxyRequestEvent request, Context context) {
        DynamoDBMapper mapper = this.initDynamoDbClient();

        String body = request.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            User user = objectMapper.readValue(body, User.class);
            mapper.save(user);
            String jsonString = objectMapper.writeValueAsString(user);
            return new APIGatewayProxyResponseEvent().withStatusCode(201).withBody(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500);
        }
    }

    public APIGatewayProxyResponseEvent read(APIGatewayProxyRequestEvent request, Context context) {
        DynamoDBMapper mapper = this.initDynamoDbClient();

        String[] arr = request.getPath().split("/");
        String id = arr[2];
        User user = mapper.load(User.class, id);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonInString = objectMapper.writeValueAsString(user);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withHeaders(headers).withBody(jsonInString);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500);
        }
    }

    public APIGatewayProxyResponseEvent update(APIGatewayProxyRequestEvent request, Context context) {
        DynamoDBMapper mapper = this.initDynamoDbClient();
        String body = request.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        User user;

        try {
            user = objectMapper.readValue(body, User.class);
            mapper.save(user);
            String jsonString = objectMapper.writeValueAsString(user);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500);
        }
    }

    public APIGatewayProxyResponseEvent delete(APIGatewayProxyRequestEvent request, Context context) {
        DynamoDBMapper mapper = this.initDynamoDbClient();
        String[] arr = request.getPath().split("/");
        String id = arr[2];
        User user = mapper.load(User.class, id);
        if(user != null)
            mapper.delete(user);

        return new APIGatewayProxyResponseEvent().withStatusCode(200);
    }

    public APIGatewayProxyResponseEvent getAll(APIGatewayProxyRequestEvent request, Context context){
        DynamoDBMapper mapper = this.initDynamoDbClient();
        List<User> userList = mapper.scan(User.class, new DynamoDBScanExpression());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonInString = objectMapper.writeValueAsString(userList);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withHeaders(headers).withBody(jsonInString);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500);
        }
    }
}

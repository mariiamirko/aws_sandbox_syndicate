package com.task10;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task10.model.*;
import com.task10.service.CognitoService;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.*;
import java.util.stream.Collectors;

import static com.task10.utilities.Constants.*;
import static com.task10.utilities.Validator.validateDateFormat;
import static com.task10.utilities.Validator.validateTimeFormat;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED)
public class ApiHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private static final String TABLE_TABLE_NAME = "cmtr-6245e71b-Tables-test";
    private static final String RESERVATION_TABLE_NAME = "cmtr-6245e71b-Reservations-test";
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private CognitoService cognitoService = new CognitoService(CognitoIdentityProviderClient.create());


    @SneakyThrows
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> event, Context context) {

        final Map<String, String> pathParameters = (Map<String, String>) event.get("pathParameters");
        final String resourcePath = (String) event.get("resource");
        final String httpMethod = (String) event.get("httpMethod");

        if (httpMethod.equals("GET")) {
            if (resourcePath.equals(TABLES_PATH)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withBody(objectMapper.writeValueAsString(getAllTables()));
            } else if (resourcePath.equals(RESERVATIONS_PATH)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withBody(objectMapper.writeValueAsString(getAllReservations()));
            } else {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withBody(objectMapper.writeValueAsString(getTableById(pathParameters)));
            }
        } else if (httpMethod.equals("POST")) {
            if (resourcePath.equals(SIGNUP_PATH)) {
                try {
                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(HttpStatus.SC_OK)
                            .withBody(cognitoService.signUpUser(event));
                } catch (RuntimeException e) {
                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                            .withBody(e.getMessage());
                }
            } else if (resourcePath.equals(SIGNIN_PATH)) {
                try {
                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(HttpStatus.SC_OK)
                            .withBody(objectMapper.writeValueAsString(cognitoService.signInUser(event)));
                } catch (RuntimeException e) {
                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                            .withBody(e.getMessage());
                }
            } else if (resourcePath.equals(TABLES_PATH)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withBody(objectMapper.writeValueAsString(postTable(event)));
            } else if (resourcePath.equals(RESERVATIONS_PATH)) {
                try {
                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(HttpStatus.SC_OK)
                            .withBody(objectMapper.writeValueAsString(postReservations(event)));
                } catch (Exception e) {
                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                            .withBody(e.getMessage());
                }
            }
        }
        return null;
    }

    private PostTablesResponse postTable(final Map<String, Object> event) {
        final Map<String, Object> inputBody = Jackson.fromJsonString((String) event.get("body"), Map.class);
        final String id = String.valueOf((int) inputBody.get(ID));
        final Map<String, AttributeValue> itemToPut = Map.of(ID, new AttributeValue().withN(id), NUMBER, new AttributeValue().withN(String.valueOf((int) inputBody.get(NUMBER))), PLACES, new AttributeValue().withN(String.valueOf((int) inputBody.get(PLACES))), IS_VIP, new AttributeValue().withBOOL((boolean) inputBody.get(IS_VIP)), MIN_ORDER, new AttributeValue().withN(String.valueOf((int) inputBody.get(MIN_ORDER))));

        final PutItemRequest request = new PutItemRequest().withTableName(TABLE_TABLE_NAME);
        request.setItem(itemToPut);

        client.putItem(request);
        return PostTablesResponse.builder().id(Integer.parseInt(id)).build();
    }

    private GetTablesResponse getAllTables() {
        final ScanRequest scanRequest = new ScanRequest().withTableName(TABLE_TABLE_NAME);
        final List<Table> tables = client.scan(scanRequest).getItems().stream()
                .map(table -> Table.builder().id(Integer.parseInt(table.get(ID).getN())).number(Integer.parseInt(table.get(NUMBER).getN())).places(Integer.parseInt(table.get(PLACES).getN())).isVip(Boolean.parseBoolean(table.get(IS_VIP).getS())).minOrder(Integer.parseInt(table.get(MIN_ORDER).getN())).build()).collect(Collectors.toList());
        return GetTablesResponse.builder().tables(tables).build();
    }

    private GetReservationsResponse getAllReservations() {
        final ScanRequest scanRequest = new ScanRequest().withTableName(RESERVATION_TABLE_NAME);
        final List<Reservation> reservations = client.scan(scanRequest).getItems().stream().map(reservation -> Reservation.builder().tableNumber(Integer.parseInt(reservation.get(TABLE_NUMBER).getS())).clientName(reservation.get(CLIENT_NAME).getS()).phoneNumber(reservation.get(PHONE_NUMBER).getS()).date(reservation.get(DATE).getS()).slotTimeStart(reservation.get(SLOT_TIME_START).getS()).slotTimeEnd(reservation.get(SLOT_TIME_END).getS())
                .build()).collect(Collectors.toList());
        return GetReservationsResponse.builder().reservations(reservations)
                .build();
    }

    private Table getTableById(final Map<String, String> pathParameters) {
//        final String tableId = pathParameters.get("tableId");
//        final Map<String, AttributeValue> keyToGet = Map.of(ID, new AttributeValue().withN(tableId));
//
//        final GetItemRequest request = new GetItemRequest().withKey(keyToGet).withTableName(TABLE_TABLE_NAME);
//        final GetItemResult result = client.getItem(request);
//
//        return Table.builder().id(Integer.parseInt(result.getItem().get(ID).getN())).minOrder(Integer.parseInt(result.getItem().get(MIN_ORDER).getN())).isVip(Boolean.parseBoolean(String.valueOf(result.getItem().get(IS_VIP).getBOOL()))).places(Integer.parseInt(result.getItem().get(PLACES).getN())).number(Integer.parseInt(result.getItem().get(NUMBER).getN()))
//                .build();
        return getAllTables().getTables().stream().filter(table -> table.getId() == Integer.parseInt(pathParameters.get("tableId"))).findFirst().orElseThrow(() -> new IllegalArgumentException("Table not found"));
    }

    private PostReservationsResponse postReservations(final Map<String, Object> event) throws RuntimeException {
        final String reservationId = UUID.randomUUID().toString();
        final Map<String, Object> inputBody = Jackson.fromJsonString((String) event.get("body"), Map.class);
        final AttributeValue tableNumber = new AttributeValue(String.valueOf((int) inputBody.get(TABLE_NUMBER)));
        final Map<String, AttributeValue> item = Map.of(ID, new AttributeValue(reservationId), TABLE_NUMBER, tableNumber, CLIENT_NAME, new AttributeValue((String) inputBody.get(CLIENT_NAME)), PHONE_NUMBER, new AttributeValue((String) inputBody.get(PHONE_NUMBER)), DATE, new AttributeValue(validateDateFormat((String) inputBody.get(DATE))), SLOT_TIME_START, new AttributeValue(validateTimeFormat((String) inputBody.get(SLOT_TIME_START))), SLOT_TIME_END, new AttributeValue(validateTimeFormat((String) inputBody.get(SLOT_TIME_END))));

        tableExists((int)inputBody.get(TABLE_NUMBER));

        final Reservation reservation = Reservation.builder().tableNumber((Integer) inputBody.get(TABLE_NUMBER)).clientName((String) inputBody.get(CLIENT_NAME)).phoneNumber((String) inputBody.get(PHONE_NUMBER)).date((String) inputBody.get(DATE)).slotTimeStart((String) inputBody.get(SLOT_TIME_START)).slotTimeEnd((String) inputBody.get(SLOT_TIME_END))
                .build();
        getAllReservations().getReservations().stream().filter(resrv -> resrv.equals(reservation)).findFirst().ifPresent(e -> {
            throw new IllegalArgumentException("Such reservation existed: " + reservation);
        });


        final PutItemRequest putItemRequest = new PutItemRequest().withTableName(RESERVATION_TABLE_NAME);
        putItemRequest.setItem(item);

        client.putItem(putItemRequest);

        return PostReservationsResponse.builder().reservationId(reservationId)
                .build();
    }

    private void tableExists(int tableNumber) {
        System.out.println("Checking if table exists");
        getAllTables().getTables().stream().filter(table -> table.getNumber() == tableNumber).findFirst().orElseThrow(() -> new IllegalArgumentException("Table does not exist"));
    }
}


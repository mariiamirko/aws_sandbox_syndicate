package com.task05;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.task05.model.EventDTO;
import com.task05.model.RequestDTO;
import com.task05.model.ResponseDTO;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
//@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "Events")
public class ApiHandler implements RequestHandler<RequestDTO, ResponseDTO> {

	public ResponseDTO handleRequest(RequestDTO request, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log(request.toString());
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
				.withRegion(Regions.EU_CENTRAL_1)
				.build();

		EventDTO event = EventDTO.builder()
				.id(UUID.randomUUID().toString())
				.principalId(request.getPrincipalId())
				.createdAt(Instant.now().toString())
				.body(request.getContent())
				.build();

		PutItemRequest putItemRequest = new PutItemRequest();
		putItemRequest.withTableName("cmtr-6245e71b-Events-test")
				.setItem(Map.of("id", new AttributeValue().withS(event.getId()),
						"principalId", new AttributeValue().withN(String.valueOf(event.getPrincipalId())),
						"createdAt", new AttributeValue().withS(event.getCreatedAt()),
						"body", new AttributeValue().withM(event.getBody().entrySet()
								.stream()
								.collect(Collectors.toMap(Map.Entry::getKey,
										entry -> new AttributeValue(entry.getValue()))))
				));
		client.putItem(putItemRequest);
		logger.log(event.toString());
		return ResponseDTO.builder()
				.statusCode(201)
				.event(event)
				.build();
	}
}

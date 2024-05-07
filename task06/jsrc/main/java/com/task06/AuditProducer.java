package com.task06;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
		roleName = "audit_producer-role",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "Audit")
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 10)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {
	private static final String TABLE_NAME = "cmtr-6245e71b-Audit-test";
	private static final String VALUE_ATTRIBUTE = "value";
	private static final String KEY_ATTRIBUTE = "key";
	private static final String UPDATED_ATTRIBUTE = "updatedAttribute";
	private static final String NEW_VALUE_ATTRIBUTE = "newValue";
	private static final String OLD_VALUE_ATTRIBUTE = "oldValue";
	private static final String MODIFICATION_TIME_ATTRIBUTE = "modificationTime";
	private static final String ITEM_KEY_ATTRIBUTE = "itemKey";
	private static final String ID_ATTRIBUTE = "id";

	public Void handleRequest(DynamodbEvent request, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log(request.toString());

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
				.withRegion(Regions.EU_CENTRAL_1)
				.build();

		for (DynamodbEvent.DynamodbStreamRecord dynamodbStreamRecord : request.getRecords()) {
			PutItemRequest putItemRequest = new PutItemRequest();
			putItemRequest.withTableName(TABLE_NAME).setItem(createAuditItem(dynamodbStreamRecord));

			logger.log(putItemRequest.toString());

			client.putItem(putItemRequest);
		}

		return null;
	}

	private static Map<String, AttributeValue> createAuditItem(DynamodbEvent.DynamodbStreamRecord streamRecord) {
		Map<String, AttributeValue> auditItem = new HashMap<>();
		auditItem.put(ID_ATTRIBUTE, new AttributeValue().withS(UUID.randomUUID().toString()));
		auditItem.put(ITEM_KEY_ATTRIBUTE,
				new AttributeValue().withS(streamRecord.getDynamodb().getKeys().get(KEY_ATTRIBUTE).getS()));
		auditItem.put(MODIFICATION_TIME_ATTRIBUTE, new AttributeValue().withS(Instant.now().toString()));
		if (streamRecord.getEventName().equals("INSERT"))
			auditItem.put(NEW_VALUE_ATTRIBUTE, new AttributeValue().withM(Map.of(
					KEY_ATTRIBUTE,
					new AttributeValue().withS(streamRecord.getDynamodb().getNewImage().get(KEY_ATTRIBUTE).getS()),
					VALUE_ATTRIBUTE,
					new AttributeValue().withN(streamRecord.getDynamodb().getNewImage().get(VALUE_ATTRIBUTE).getN())
			)));
		else if (streamRecord.getEventName().equals("MODIFY")) {
			auditItem.put(UPDATED_ATTRIBUTE, new AttributeValue().withS(VALUE_ATTRIBUTE));
			auditItem.put(OLD_VALUE_ATTRIBUTE,
					new AttributeValue().withN(streamRecord.getDynamodb().getOldImage().get(VALUE_ATTRIBUTE).getN()));
			auditItem.put(NEW_VALUE_ATTRIBUTE,
					new AttributeValue().withN(streamRecord.getDynamodb().getNewImage().get(VALUE_ATTRIBUTE).getN()));
		}
		return auditItem;
	}
}

package com.task09;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.lib.OpenMeteoSdkLib.getLatestForecast;
import static com.syndicate.deployment.model.TracingMode.Active;

@LambdaHandler(lambdaName = "processor",
		roleName = "processor-role",
		tracingMode = Active,
		isPublishVersion = true,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = { "lib/OpenMeteoSdkLib.jar" },
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@DependsOn(
		name = "sdk-layer",
		resourceType = ResourceType.LAMBDA_LAYER
)
@DependsOn(
		name = "Weather",
		resourceType = ResourceType.DYNAMODB_TABLE
)
public class Processor implements RequestHandler<Object, Map<String, Object>> {
	private static final String TABLE_NAME = "cmtr-6245e71b-Weather-test";

	public Map<String, Object> handleRequest(Object request, Context context) {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
				.withRegion(Regions.EU_CENTRAL_1)
				.build();
		DynamoDB dynamoDB = new DynamoDB(client);
		Table table = dynamoDB.getTable(TABLE_NAME);
		String forecastString = getLatestForecast();

		table.putItem(createItem(forecastString));

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("statusCode", 201);
		resultMap.put("event", forecastString);
		return resultMap;
	}

	private Item createItem(String forecastString) {
		Item item = new Item();
		item.withString("id", UUID.randomUUID().toString());
		item.withJSON("forecast", forecastString);
		return item;
	}
}


package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sqs_handler",
	roleName = "sqs_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(targetQueue = "async_queue", batchSize = 100)
@DependsOn(resourceType = ResourceType.SQS_QUEUE, name = "async_queue")
public class SqsHandler implements RequestHandler<SQSEvent, Void > {

	public Void handleRequest(SQSEvent sqsEvent, Context context) {
		LambdaLogger logger = context.getLogger();
		for(SQSEvent.SQSMessage message: sqsEvent.getRecords()){
			logger.log(message.getBody());
		}
		return null;
	}
}

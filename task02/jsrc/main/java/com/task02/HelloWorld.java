package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

@LambdaHandler(lambdaName = "hello_world",
		roleName = "hello_world-role",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final Gson gson = new Gson();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent,
			Context context) {
		if (apiGatewayProxyRequestEvent.getRequestContext().getPath().equals("/hello")) {
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(gson.toJson(new HelloMessage("Hello from Lambda", 200)));
		}
		else
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(400)
					.withBody(String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
							apiGatewayProxyRequestEvent.getPath(), apiGatewayProxyRequestEvent.getRequestContext().getHttpMethod()));
	}
}

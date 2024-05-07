package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import org.json.JSONObject;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@LambdaHandler(lambdaName = "uuid_generator",
        roleName = "uuid_generator-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(targetRule = "uuid_trigger")
public class UuidGenerator implements RequestHandler<Object, Void> {
    private static final String BUCKET_NAME = "cmtr-6245e71b-uuid-storage-test";

    public Void handleRequest(Object request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(request.toString());

        try (S3Client client = S3Client.create()) {
            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(Instant.now().toString()).build();

            List<String> uuids = Stream.generate(() -> UUID.randomUUID().toString())
                    .limit(10)
                    .collect(Collectors.toList());

            JSONObject uuidsJson = new JSONObject();
            uuidsJson.put("ids", uuids);

            client.putObject(putObjectRequest, RequestBody.fromString(uuidsJson.toString()));
        }
        return null;
    }
}

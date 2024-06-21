/*
 * Copyright 2024 American Well Systems.
 * All rights reserved.
 *
 * It is illegal to use, reproduce or distribute
 * any part of this Intellectual Property without
 * prior written authorization from American Well.
 */

package com.task10.service;

import com.amazonaws.util.json.Jackson;
import com.task10.model.SignInResponse;
import com.task10.model.User;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

public class CognitoService {
    private static final String COGNITO_USER_POOL_NAME = "cmtr-6245e71b-simple-booking-userpool-test";
    private final CognitoIdentityProviderClient cognitoClient;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public String signUpUser(final Map<String, Object> input) {
        try {
            final User userFromEvent = getUserFromEvent(input);

            final SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(getClientId()).username(userFromEvent.getEmail()).password(userFromEvent.getPassword()).userAttributes(AttributeType.builder().name("given_name").value(userFromEvent.getFirstName()).build(), AttributeType.builder().name("family_name").value(userFromEvent.getLastName()).build(), AttributeType.builder().name("email").value(userFromEvent.getEmail()).build()).build();

            cognitoClient.signUp(signUpRequest);

            final AdminConfirmSignUpRequest adminConfirmSignUpRequest = AdminConfirmSignUpRequest.builder().userPoolId(getUserPoolId(COGNITO_USER_POOL_NAME)).username(userFromEvent.getEmail()).build();

            cognitoClient.adminConfirmSignUp(adminConfirmSignUpRequest);
            return "User Created!";
        } catch (Exception e) {
            throw e;
        }
    }

    public SignInResponse signInUser(final Map<String, Object> input) {
        try {
            final User user = getUserFromEvent(input);
            final String userPoolId = getUserPoolId(COGNITO_USER_POOL_NAME);
            final AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .userPoolId(userPoolId)
                    .authParameters(Map.of("USERNAME", user.getEmail(), "PASSWORD", user.getPassword()))
                    .clientId(getClientId())
                    .build();

            final AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);

            final AuthenticationResultType authResult = authResponse.authenticationResult();
            return SignInResponse.builder().accessToken(authResult.idToken()).build();
        } catch (Exception e) {
            throw e;
        }
    }

    private String getUserPoolId(final String userPoolName) {
        try {
            final ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder().maxResults(60)
                    .build();

            final ListUserPoolsResponse listUserPoolsResponse = cognitoClient.listUserPools(listUserPoolsRequest);

            for (UserPoolDescriptionType userPool : listUserPoolsResponse.userPools()) {
                if (userPool.name().equals(userPoolName)) {
                    return userPool.id();
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    private String getClientId() {
        final ListUserPoolClientsRequest userPoolClientsRequest = ListUserPoolClientsRequest.builder().userPoolId(getUserPoolId(COGNITO_USER_POOL_NAME)).build();

        return cognitoClient.listUserPoolClients(userPoolClientsRequest).userPoolClients().stream().filter(c -> c.clientName().equals("client-app")).findFirst().map(UserPoolClientDescription::clientId).orElseThrow(() -> new RuntimeException("Client ID not found"));
    }

    private User getUserFromEvent(final Map<String, Object> input) {
        final Map<String, Object> inputBody = Jackson.fromJsonString((String) input.get("body"), Map.class);
        return User.builder().email((String) inputBody.get("email")).firstName((String) inputBody.get("firstName")).lastName((String) inputBody.get("firstName")).password((String) inputBody.get("password")).build();
    }
}

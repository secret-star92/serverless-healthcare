package com.amazonaws.healthcare.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.model.SignIn;
import com.amazonaws.healthcare.util.JsonUtil;
import com.amazonaws.healthcare.util.StatusCode;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.Base64;
import com.amazonaws.util.StringUtils;

public class PostSignInHandler implements RequestStreamHandler {
	private static final String REGION = System.getenv("REGION");
	private static final String CLIENT_APP_ID = System.getenv("CLIENT_APP_ID");
	private static final String SECRET_HASH = System.getenv("SECRET_HASH");
	private final static String USER_POOL_ID = System.getenv("USER_POOL_ID");
	private final static String HMAC_SHA_256 = "HmacSHA256";

	
	@Override

	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

		LambdaLogger logger = context.getLogger();
		
		AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClient.builder().withRegion(Regions.fromName(REGION)).build();
				//.withCredentials(new Annony).withRegion(Regions.fromName(REGION)).build();

		Set<String> errorMessages = new LinkedHashSet<>();

		logger.log(String.format("Cognito REGION %s , CLIENT_APP_ID %s ", REGION, CLIENT_APP_ID));
		ServerlessOutput serverlessOutput = new ServerlessOutput();
		try {
			ServerlessInput serverlessInput = JsonUtil.parseObjectFromStream(input, ServerlessInput.class);

			String signInStr = (String) serverlessInput.getBody();
			SignIn signin = JsonUtil.parseObjectFromBytes(signInStr.getBytes(), SignIn.class);

			boolean isValid = new EntityValidator<>().validate.isValid(signin, errorMessages);

			if (isValid) {

				
				AdminInitiateAuthRequest adminInitiateAuthRequest = new AdminInitiateAuthRequest();
				adminInitiateAuthRequest.setAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH);
				adminInitiateAuthRequest.setClientId(CLIENT_APP_ID);
				adminInitiateAuthRequest.setUserPoolId(USER_POOL_ID);

				Map<String, String> authParamMap = new HashMap<>();
				authParamMap.put("USERNAME", signin.getUsername());
				authParamMap.put("PASSWORD", signin.getPassword());
				authParamMap.put("SECRET_HASH", getSecretHash(signin.getUsername(), CLIENT_APP_ID, SECRET_HASH));

				adminInitiateAuthRequest.setAuthParameters(authParamMap);
				

				AdminInitiateAuthResult adminInitiateAuthResult = cognitoIdentityProvider
						.adminInitiateAuth(adminInitiateAuthRequest);
				AuthenticationResultType authenticationResultType = adminInitiateAuthResult.getAuthenticationResult();

				serverlessOutput.setStatusCode(StatusCode.SUCCESS.getCode());
				serverlessOutput.setBody(JsonUtil.convertToString(authenticationResultType));
			} else {
				serverlessOutput.setStatusCode(StatusCode.VALICATION_FAILED.getCode());
				serverlessOutput.setBody("Invalid username and password!");
				logger.log(String.format("Validation failed %s", errorMessages));
			}

		} catch (Exception exe) {
			serverlessOutput.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR.getCode());
			serverlessOutput.setBody("Invalid username and password!");
			logger.log(String.format("internal error occurred  %s", exe.toString()));
			throw exe;

		} finally {
			try {
				JsonUtil.writeOutput(output, serverlessOutput);
			} catch (IOException exe) {
				serverlessOutput.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR.getCode());
				serverlessOutput.setBody(exe.getMessage());
				logger.log(String.format("internal error occurred  %s", exe.toString()));
				throw exe;
			}
		}
	}

	public static String getSecretHash(String username, String clientId, String clientSecret) {
		if (username == null) {
			throw new IllegalArgumentException("username cannot be null");
		}
		if (clientId == null) {
			throw new IllegalArgumentException("client ID cannot be null");
		}
		if (clientSecret == null) {
			return null;
		}
		SecretKeySpec signingKey = new SecretKeySpec(clientSecret.getBytes(StringUtils.UTF8), HMAC_SHA_256);
		try {
			Mac mac = Mac.getInstance(HMAC_SHA_256);
			mac.init(signingKey);
			mac.update(username.getBytes(StringUtils.UTF8));
			byte[] rawHmac = mac.doFinal(clientId.getBytes(StringUtils.UTF8));
			return new String(Base64.encode(rawHmac));
		} catch (Exception e) {
			throw new RuntimeException("errors in HMAC calculation");
		}
	}

}

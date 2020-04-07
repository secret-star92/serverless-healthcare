package com.amazonaws.healthcare.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.model.User;
import com.amazonaws.healthcare.util.JsonUtil;
import com.amazonaws.healthcare.util.StatusCode;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.SignUpRequest;
import com.amazonaws.services.cognitoidp.model.SignUpResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.Base64;
import com.amazonaws.util.StringUtils;

public class PostSignUpHandler implements RequestStreamHandler {
	private String POOL_ID;
	// private String CLIENT_APP_ID;
	private String FED_POOL_ID;
	private String CUSTOMDOMAIN;
	
	// DynamoDB table name for storing Provider metadata.
	private static final String CLIENT_APP_ID = System.getenv("CLIENT_APP_ID");
    private static final String USER_POOL_ID= System.getenv("USER_POOL_ID");
	private static final String SECRET_HASH = System.getenv("SECRET_HASH");
	private final static String HMAC_SHA_256 = "HmacSHA256";

	private static final String UTF8_CHARSET = "UTF-8";

	AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder.standard()
			.withCredentials(new ProfileCredentialsProvider("default"))
			.withRegion(Regions.fromName(Constants.REGION)).build();
	
	@Override

	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

		LambdaLogger logger = context.getLogger();

		Set<String> errorMessages = new LinkedHashSet<>();

		logger.log(String.format("Cognito REGION %s , CLIENT_APP_ID %s ", Constants.REGION, CLIENT_APP_ID));
		ServerlessOutput serverlessOutput = new ServerlessOutput();
		try {
			ServerlessInput serverlessInput = JsonUtil.parseObjectFromStream(input, ServerlessInput.class);

			String userStr = (String) serverlessInput.getBody();
			User user = JsonUtil.parseObjectFromBytes(userStr.getBytes(), User.class);

			boolean isValid = new EntityValidator<>().validate.isValid(user, errorMessages);

			if (isValid) {
			//	ClasspathPropertiesFileCredentialsProvider propertiesFileCredentialsProvider = 
				//           new ClasspathPropertiesFileCredentialsProvider();
				//AnonymousAWSCredentials awsCreds = new AnonymousAWSCredentials();
	

				/*
				 * AWSCognitoIdentityProvider cognitoIdentityProvider =
				 * AWSCognitoIdentityProviderClientBuilder.de .withCredentials(new
				 * AWSStaticCredentialsProvider(awsCreds))
				 * .withRegion(Regions.fromName(REGION)).build();
				 */
				SignUpRequest signUpRequest = new SignUpRequest();
				signUpRequest.setClientId(CLIENT_APP_ID);
				signUpRequest.setUsername(user.getUsername());
				signUpRequest.setPassword(user.getPassword());
				List<AttributeType> list = new ArrayList<>();
				//list.add(convertToAttributeType("phone_numbe", user.getPhoneNumber()));
				list.add(convertToAttributeType("email", user.getEmail()));

				signUpRequest.setSecretHash(getSecretHash(user.getUsername(), CLIENT_APP_ID, SECRET_HASH));
				signUpRequest.setUserAttributes(list);

				SignUpResult result = cognitoIdentityProvider.signUp(signUpRequest);

				System.out.println(result);

				serverlessOutput.setStatusCode(StatusCode.SUCCESS.getCode());
				serverlessOutput.setBody("true");
			} else {
				serverlessOutput.setStatusCode(StatusCode.VALICATION_FAILED.getCode());
				serverlessOutput.setBody(JsonUtil.convertToString(errorMessages));
				logger.log(String.format("Validation failed %s", errorMessages));
			}

		} catch (Exception exe) {
			serverlessOutput.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR.getCode());
			serverlessOutput.setBody("false");
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

	 void login(String userId, String password) {
		AdminInitiateAuthRequest adminInitiateAuthRequest = new AdminInitiateAuthRequest();
		adminInitiateAuthRequest.setAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH);
		adminInitiateAuthRequest.setClientId(CLIENT_APP_ID);
		adminInitiateAuthRequest.setUserPoolId(USER_POOL_ID);

		Map<String, String> authParamMap = new HashMap<>();
		authParamMap.put("USERNAME", userId);
		authParamMap.put("PASSWORD", password);
		authParamMap.put("SECRET_HASH", getSecretHash(userId,  CLIENT_APP_ID, SECRET_HASH));

		adminInitiateAuthRequest.setAuthParameters(authParamMap);

		AdminInitiateAuthResult adminInitiateAuthResult = cognitoIdentityProvider.adminInitiateAuth(adminInitiateAuthRequest);
		AuthenticationResultType authenticationResultType = adminInitiateAuthResult.getAuthenticationResult();
		System.out.println("id_token=" + authenticationResultType.getIdToken());
		System.out.println("access_token=" + authenticationResultType.getAccessToken());
		System.out.println("refresh_token=" + authenticationResultType.getRefreshToken());
		// System.out.println("Expires in " + authenticationResultType.getExpiresIn());

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

	/*
	 * private String hmac(String stringToSign) { String signature = null; byte[]
	 * data; byte[] rawHmac; try { data = stringToSign.getBytes(UTF8_CHARSET);
	 * //rawHmac = mac.doFinal(data); Encoder encoder = Base64.getUrlEncoder();
	 * signature = new String(encoder.encode(data)); } catch
	 * (UnsupportedEncodingException e) { throw new RuntimeException(UTF8_CHARSET +
	 * " is unsupported!", e); } return signature; }
	 */
	private AttributeType convertToAttributeType(String name, String value) {
		AttributeType attributeType = new AttributeType();
		attributeType.setName(name);
		attributeType.setValue(value);
		return attributeType;
	}
}

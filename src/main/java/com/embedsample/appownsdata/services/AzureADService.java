package com.embedsample.appownsdata.services;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embedsample.appownsdata.config.Config;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;

public class AzureADService {
	
	static final Logger logger = LoggerFactory.getLogger(AzureADService.class);
	
	private AzureADService () {
		throw new IllegalStateException("Authentication service class");
	}
	
	public static String getAccessToken() throws MalformedURLException, InterruptedException, ExecutionException {
		
		if (Config.authenticationType.equalsIgnoreCase("MasterUser")) {
			return getAccessTokenUsingMasterUser(Config.clientId, Config.pbiUsername, Config.pbiPassword);
		} else if (Config.authenticationType.equalsIgnoreCase("ServicePrincipal")) {
			if (Config.tenantId.isEmpty()) {
				throw new RuntimeException("Tenant Id is empty");
			} 
			return getAccessTokenUsingServicePrincipal(Config.clientId, Config.tenantId, Config.appSecret);
		} else {	
			throw new RuntimeException("Invalid authentication type: " + Config.authenticationType);
		}
	}
	private static String getAccessTokenUsingServicePrincipal(String clientId, String tenantId, String appSecret) throws MalformedURLException, InterruptedException, ExecutionException {
		
		ConfidentialClientApplication app = ConfidentialClientApplication.builder(
					clientId,
					ClientCredentialFactory.createFromSecret(appSecret))
					.authority(Config.authorityUrl + tenantId)
					.build();
		
		ClientCredentialParameters clientCreds = ClientCredentialParameters.builder(
				Collections.singleton(Config.scopeUrl))
				.build();
		
		IAuthenticationResult result = app.acquireToken(clientCreds).get();
		
		if (result != null && result.accessToken() != null && !result.accessToken().isEmpty()) {
			if (Config.DEBUG) {
				logger.info("Authenticated with Service Principal mode");
			}
			return result.accessToken();
		} else {
			logger.error("Failed to authenticate with Service Principal mode");
			return null;
		}
	}

	private static String getAccessTokenUsingMasterUser(String clientId, String username, String password) throws MalformedURLException, InterruptedException, ExecutionException {
		
		PublicClientApplication app = PublicClientApplication.builder(clientId)
				.authority(Config.authorityUrl + "organizations")
				.build();
		
		UserNamePasswordParameters userCreds = UserNamePasswordParameters.builder(
				Collections.singleton(Config.scopeUrl),
				username,
				password.toCharArray()).build();
		
		IAuthenticationResult result = app.acquireToken(userCreds).get();
		
		if (result != null && result.accessToken() != null && !result.accessToken().isEmpty()) {
			if (Config.DEBUG) {
				logger.info("Authenticated with MasterUser mode");
			}
			return result.accessToken();
		} else {
			logger.error("Failed to authenticate with MasterUser mode");
			return null;
		}
	}
}
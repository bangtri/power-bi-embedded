package com.embedsample.appownsdata.services;

import com.embedsample.appownsdata.config.Config;
import com.embedsample.appownsdata.models.EmbedConfig;
import com.embedsample.appownsdata.models.ReportConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.embedsample.appownsdata.models.EmbedToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class PowerBIService {
	
	static final Logger logger = LoggerFactory.getLogger(PowerBIService.class);
	
	@SuppressWarnings("unused")
	private static JSONObject responseHeader;

	private PowerBIService () {
		throw new IllegalStateException("Power BI service class");
	}
		
	public static EmbedConfig getEmbedConfig(String accessToken, String workspaceId, String reportId, String... additionalDatasetIds) throws JsonMappingException, JsonProcessingException {
		if (reportId == null || reportId.isEmpty()) {
			throw new RuntimeException("Empty Report Id");
		}
		if (workspaceId == null || workspaceId.isEmpty()) {
			throw new RuntimeException("Empty Workspace Id");
		}
		StringBuilder urlStringBuilder = new StringBuilder("https://api.powerbi.com/v1.0/myorg/groups/"); 
		urlStringBuilder.append(workspaceId);
		urlStringBuilder.append("/reports/");
		urlStringBuilder.append(reportId);
		HttpHeaders reqHeader = new HttpHeaders();
		reqHeader.put("Content-Type", Arrays.asList("application/json"));
		reqHeader.put("Authorization", Arrays.asList("Bearer " + accessToken));
		HttpEntity<String> reqEntity = new HttpEntity<> (reqHeader);
		String endPointUrl = urlStringBuilder.toString();
		RestTemplate getReportRestTemplate = new RestTemplate();
		ResponseEntity<String> response = getReportRestTemplate.exchange(endPointUrl, HttpMethod.GET, reqEntity, String.class);
		HttpHeaders responseHeader = response.getHeaders();
		String responseBody = response.getBody();
		EmbedConfig reportEmbedConfig = new EmbedConfig();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ReportConfig embedReport = mapper.readValue(responseBody, ReportConfig.class);
		reportEmbedConfig.embedReports = new ArrayList<ReportConfig>();
		reportEmbedConfig.embedReports.add(embedReport);
		if (Config.DEBUG) {
			List<String> reqIdList = responseHeader.get("RequestId");
			logger.info("Retrieved report details");
			if (reqIdList != null && !reqIdList.isEmpty()) {
				for (String reqId: reqIdList) {
					logger.info("Request Id: {}", reqId);
				}
			}
		}
		JSONObject responseObj = new JSONObject(responseBody);
		List<String> datasetIds = new ArrayList<String>();
		datasetIds.add(responseObj.getString("datasetId"));
		for (String datasetId : additionalDatasetIds) {
			datasetIds.add(datasetId);
			System.out.println(datasetId);
		}
		reportEmbedConfig.embedToken = PowerBIService.getEmbedToken(accessToken, reportId, datasetIds);
		return reportEmbedConfig;
	}
	
	public static EmbedConfig getEmbedConfig(String accessToken, String workspaceId, List<String> reportIds) throws JsonMappingException, JsonProcessingException {
		if (reportIds == null || reportIds.isEmpty()) {
			throw new RuntimeException("Empty Report Ids");
		}
		if (workspaceId == null || workspaceId.isEmpty()) {
			throw new RuntimeException("Empty Workspace Id");
		}
		EmbedConfig reportEmbedConfig = new EmbedConfig();
		reportEmbedConfig.embedReports = new ArrayList<ReportConfig>();
		List<String> datasetIds = new ArrayList<String>();
		for (String reportId : reportIds) {
			StringBuilder urlStringBuilder = new StringBuilder("https://api.powerbi.com/v1.0/myorg/groups/"); 
			urlStringBuilder.append(workspaceId);
			urlStringBuilder.append("/reports/");
			urlStringBuilder.append(reportId);
			HttpHeaders reqHeader = new HttpHeaders();
			reqHeader.put("Content-Type", Arrays.asList("application/json"));
			reqHeader.put("Authorization", Arrays.asList("Bearer " + accessToken));
			HttpEntity<String> reqEntity = new HttpEntity<> (reqHeader);
			String endPointUrl = urlStringBuilder.toString();
			RestTemplate getReportRestTemplate = new RestTemplate();
			ResponseEntity<String> response = getReportRestTemplate.exchange(endPointUrl, HttpMethod.GET, reqEntity, String.class);
			HttpHeaders responseHeader = response.getHeaders();
			String responseBody = response.getBody();
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ReportConfig embedReport = mapper.readValue(responseBody, ReportConfig.class);
			reportEmbedConfig.embedReports.add(embedReport);
			if (Config.DEBUG) {
				List<String> reqIdList = responseHeader.get("RequestId");
				logger.info("Retrieved report details");
				if (reqIdList != null && !reqIdList.isEmpty()) {
					for (String reqId: reqIdList) {
						logger.info("Request Id: {}", reqId);
					}
				}
			}
			JSONObject responseObj = new JSONObject(responseBody);
			datasetIds.add(responseObj.getString("datasetId"));
		}
		reportEmbedConfig.embedToken = PowerBIService.getEmbedToken(accessToken, reportIds, datasetIds);
		return reportEmbedConfig;
	}
	
	public static EmbedConfig getEmbedConfig(String accessToken, String workspaceId, List<String> reportIds, List<String> additionalDatasetIds) throws JsonMappingException, JsonProcessingException {
		if (reportIds == null || reportIds.isEmpty()) {
			throw new RuntimeException("Empty Report Ids");
		}
		if (workspaceId == null || workspaceId.isEmpty()) {
			throw new RuntimeException("Empty Workspace Id");
		}
		EmbedConfig reportEmbedConfig = new EmbedConfig();
		reportEmbedConfig.embedReports = new ArrayList<ReportConfig>();
		for (String reportId : reportIds) {
			StringBuilder urlStringBuilder = new StringBuilder("https://api.powerbi.com/v1.0/myorg/groups/"); 
			urlStringBuilder.append(workspaceId);
			urlStringBuilder.append("/reports/");
			urlStringBuilder.append(reportId);
			HttpHeaders reqHeader = new HttpHeaders();
			reqHeader.put("Content-Type", Arrays.asList("application/json"));
			reqHeader.put("Authorization", Arrays.asList("Bearer " + accessToken));
			HttpEntity<String> reqEntity = new HttpEntity<> (reqHeader);
			String endPointUrl = urlStringBuilder.toString();
			RestTemplate getReportRestTemplate = new RestTemplate();
			ResponseEntity<String> response = getReportRestTemplate.exchange(endPointUrl, HttpMethod.GET, reqEntity, String.class);
			HttpHeaders responseHeader = response.getHeaders();
			String responseBody = response.getBody();
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ReportConfig embedReport = mapper.readValue(responseBody, ReportConfig.class);
			reportEmbedConfig.embedReports.add(embedReport);
			if (Config.DEBUG) {
				List<String> reqIdList = responseHeader.get("RequestId");
				logger.info("Retrieved report details");
				if (reqIdList != null && !reqIdList.isEmpty()) {
					for (String reqId: reqIdList) {
						logger.info("Request Id: {}", reqId);
					}
				}
			}
			if (additionalDatasetIds == null) {
				additionalDatasetIds = new ArrayList<String>();
			}
			JSONObject responseObj = new JSONObject(responseBody);
			additionalDatasetIds.add(responseObj.getString("datasetId"));
		}
		
		// Get embed token
		reportEmbedConfig.embedToken = PowerBIService.getEmbedToken(accessToken, reportIds, additionalDatasetIds);
		return reportEmbedConfig;
	}

	public static EmbedToken getEmbedToken(String accessToken, String reportId, List<String> datasetIds, String... targetWorkspaceIds) throws JsonMappingException, JsonProcessingException {
		final String uri = "https://api.powerbi.com/v1.0/myorg/GenerateToken";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.put("Content-Type", Arrays.asList("application/json"));
		headers.put("Authorization", Arrays.asList("Bearer " + accessToken));
		JSONArray jsonDatasets = new JSONArray();
		for (String datasetId : datasetIds) {
			jsonDatasets.put(new JSONObject().put("id", datasetId));
		}
		JSONArray jsonReports = new JSONArray();
		jsonReports.put(new JSONObject().put("id", reportId));
		JSONArray jsonWorkspaces = new JSONArray();
		for (String targetWorkspaceId: targetWorkspaceIds) {
			jsonWorkspaces.put(new JSONObject().put("id", targetWorkspaceId));
		}
		JSONObject requestBody = new JSONObject();
		requestBody.put("datasets", jsonDatasets);
		requestBody.put("reports", jsonReports);
		requestBody.put("targetWorkspaces", jsonWorkspaces);
		HttpEntity<String> httpEntity = new HttpEntity<> (requestBody.toString(), headers);
		ResponseEntity<String> response = restTemplate.postForEntity(uri, httpEntity, String.class);
		HttpHeaders responseHeader = response.getHeaders();
		String responseBody = response.getBody();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		EmbedToken embedToken = mapper.readValue(responseBody, EmbedToken.class);
		if (Config.DEBUG) {
			List<String> reqIdList = responseHeader.get("RequestId");
			logger.info("Retrieved Embed token\nEmbed Token Id: {}", embedToken.tokenId);
			if (reqIdList != null && !reqIdList.isEmpty()) {
				for (String reqId: reqIdList) {
					logger.info("Request Id: {}", reqId);
				}
			}
		}
		return embedToken;
	}
	
	public static EmbedToken getEmbedToken(String accessToken, List<String> reportIds, List<String> datasetIds, String... targetWorkspaceIds) throws JsonMappingException, JsonProcessingException {
		final String uri = "https://api.powerbi.com/v1.0/myorg/GenerateToken";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.put("Content-Type", Arrays.asList("application/json"));
		headers.put("Authorization", Arrays.asList("Bearer " + accessToken));
		JSONArray jsonDatasets = new JSONArray();
		for (String datasetId : datasetIds) {
			jsonDatasets.put(new JSONObject().put("id", datasetId));
		}
		JSONArray jsonReports = new JSONArray();
		for (String reportId : reportIds) {
			jsonReports.put(new JSONObject().put("id", reportId));
		}
		JSONObject requestBody = new JSONObject();
		requestBody.put("datasets", jsonDatasets);
		requestBody.put("reports", jsonReports);
		JSONArray jsonWorkspaces = new JSONArray();
		for (String targetWorkspaceId: targetWorkspaceIds) {
			jsonWorkspaces.put(new JSONObject().put("id", targetWorkspaceId));
		}
		requestBody.put("targetWorkspaces", jsonWorkspaces);
		HttpEntity<String> httpEntity = new HttpEntity<> (requestBody.toString(), headers);
		ResponseEntity<String> response = restTemplate.postForEntity(uri, httpEntity, String.class);
		HttpHeaders responseHeader = response.getHeaders();
		String responseBody = response.getBody();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		EmbedToken embedToken = mapper.readValue(responseBody, EmbedToken.class);
		if (Config.DEBUG) {
			List<String> reqIdList = responseHeader.get("RequestId");
			logger.info("Retrieved Embed token\nEmbed Token Id: {}", embedToken.tokenId);
			if (reqIdList != null && !reqIdList.isEmpty()) {
				for (String reqId: reqIdList) {
					logger.info("Request Id: {}", reqId);
				}
			}
		}
		return embedToken;
	}
}

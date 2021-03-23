package com.embedsample.appownsdata.controllers;

import com.embedsample.appownsdata.config.Config;
import com.embedsample.appownsdata.models.EmbedConfig;
import com.embedsample.appownsdata.services.AzureADService;
import com.embedsample.appownsdata.services.PowerBIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class EmbedController {

	static final Logger logger = LoggerFactory.getLogger(EmbedController.class);

	@GetMapping(path = "/")
	public ModelAndView embedReportHome() {
		return new ModelAndView("EmbedReport");
	}

	@GetMapping(path = "/getembedinfo")
	@ResponseBody
	public ResponseEntity<String> embedInfoController() throws JsonMappingException, JsonProcessingException {

		String accessToken;
		try {
			accessToken = AzureADService.getAccessToken();
		} catch (ExecutionException | MalformedURLException | RuntimeException ex) {
			logger.error(ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());

		} catch (InterruptedException interruptedEx) {
			logger.error(interruptedEx.getMessage());
			
			Thread.currentThread().interrupt();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(interruptedEx.getMessage());
		}

		try {
			EmbedConfig reportEmbedConfig = PowerBIService.getEmbedConfig(accessToken, Config.workspaceId, Config.reportId);
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < reportEmbedConfig.embedReports.size(); i++) {
				jsonArray.put(reportEmbedConfig.embedReports.get(i).getJSONObject());
			}
			JSONObject responseObj = new JSONObject();
			responseObj.put("embedToken", reportEmbedConfig.embedToken.token);
			responseObj.put("embedReports", jsonArray);
			responseObj.put("tokenExpiry", reportEmbedConfig.embedToken.expiration);

			String response = responseObj.toString();
			return ResponseEntity.ok(response);

		} catch (HttpClientErrorException hcex) {
			StringBuilder errMsgStringBuilder = new StringBuilder("Error: "); 
			errMsgStringBuilder.append(hcex.getMessage());
			HttpHeaders header = hcex.getResponseHeaders();
			List<String> requestIds = header.get("requestId");
			if (requestIds != null) {
				for (String requestId: requestIds) {
					errMsgStringBuilder.append("\nRequest Id: ");
					errMsgStringBuilder.append(requestId);
				}
			}
			String errMsg = errMsgStringBuilder.toString();
			logger.error(errMsg);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errMsg);

		} catch (RuntimeException rex) {
			logger.error(rex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rex.getMessage());
		}
	}
}
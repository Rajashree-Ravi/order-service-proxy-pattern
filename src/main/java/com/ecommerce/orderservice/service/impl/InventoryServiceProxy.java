package com.ecommerce.orderservice.service.impl;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.ecommerce.orderservice.model.InventoryDto;

@Service
public class InventoryServiceProxy {

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceProxy.class);

	@Autowired
	private RestTemplate restTemplate;

	private static String INVENTORY_SERVICE_URL = "http://localhost:8082/api/inventory/";

	public InventoryDto getInventoryById(Long inventoryId) {
		String traceId = UUID.randomUUID().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.set("TRACE", traceId);

		HttpEntity<InventoryDto> httpEntity = new HttpEntity<>(null, headers);

		return processProxyRequest(INVENTORY_SERVICE_URL + inventoryId, HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<InventoryDto>() {
				}, traceId).getBody();
	}

	public InventoryDto createInventory(InventoryDto inventory) {
		String traceId = UUID.randomUUID().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("TRACE", traceId);

		HttpEntity<InventoryDto> httpEntity = new HttpEntity<>(inventory, headers);

		return processProxyRequest(INVENTORY_SERVICE_URL, HttpMethod.POST, httpEntity,
				new ParameterizedTypeReference<InventoryDto>() {
				}, UUID.randomUUID().toString()).getBody();
	}

	public InventoryDto updateInventory(long id, InventoryDto inventory) {
		String traceId = UUID.randomUUID().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("TRACE", traceId);

		HttpEntity<InventoryDto> httpEntity = new HttpEntity<>(inventory, headers);

		return processProxyRequest(INVENTORY_SERVICE_URL + id, HttpMethod.PUT, httpEntity,
				new ParameterizedTypeReference<InventoryDto>() {
				}, UUID.randomUUID().toString()).getBody();
	}

	public void deleteInventory(long id) {
		String traceId = UUID.randomUUID().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.set("TRACE", traceId);

		HttpEntity<InventoryDto> httpEntity = new HttpEntity<>(null, headers);

		String responseBody = processProxyRequest(INVENTORY_SERVICE_URL + id, HttpMethod.DELETE, httpEntity,
				new ParameterizedTypeReference<String>() {
				}, UUID.randomUUID().toString()).getBody();
		LOGGER.info(responseBody);
	}

	public List<InventoryDto> getAllInventory() {
		String traceId = UUID.randomUUID().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.set("TRACE", traceId);

		HttpEntity<InventoryDto> httpEntity = new HttpEntity<>(null, headers);

		return processProxyRequest(INVENTORY_SERVICE_URL, HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<List<InventoryDto>>() {
				}, UUID.randomUUID().toString()).getBody();
	}

	public List<InventoryDto> getInventoryByProductId(Long inventoryId) {
		String traceId = UUID.randomUUID().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.set("TRACE", traceId);

		HttpEntity<InventoryDto> httpEntity = new HttpEntity<>(null, headers);

		return processProxyRequest(INVENTORY_SERVICE_URL + "product/" + inventoryId, HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<List<InventoryDto>>() {
				}, UUID.randomUUID().toString()).getBody();
	}

	@SuppressWarnings("unchecked")
	@Retryable(exclude = {
			HttpStatusCodeException.class }, include = Exception.class, backoff = @Backoff(delay = 5000, multiplier = 4.0), maxAttempts = 4)
	public <T> ResponseEntity<T> processProxyRequest(String url, HttpMethod method, HttpEntity<InventoryDto> httpEntity,
			ParameterizedTypeReference<T> entityType, String traceId) {

		// You can add logging, metrics collection, etc. here

		ThreadContext.put("traceId", traceId);
		LOGGER.info("Trace Id : " + traceId);

		ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
		restTemplate.setRequestFactory(factory);

		try {
			ResponseEntity<T> response = restTemplate.exchange(url, method, httpEntity, entityType);

			LOGGER.info("Response Status : " + response.getStatusCode().toString());
			LOGGER.info("Response Headers : " + response.getHeaders().toString());
			LOGGER.info("Response Body : " + response.getBody().toString());

			return response;

		} catch (HttpStatusCodeException e) {
			LOGGER.error("Exception occured in calling REST API : " + url);
			LOGGER.error("Error Message : " + e.getMessage());

			return new ResponseEntity<T>((T) e.getResponseBodyAsString(), e.getResponseHeaders(), e.getRawStatusCode());
		}

	}

	@Recover
	public <T> ResponseEntity<T> recoverFromRestClientErrors(Exception e, String url, HttpMethod method,
			HttpEntity<InventoryDto> httpEntity, ParameterizedTypeReference<T> entityType, String traceId) {

		LOGGER.error("Retry method for the following url " + url + " has failed" + e.getMessage());
		LOGGER.error(e.getStackTrace().toString());

		throw new RuntimeException("There was an error trying to process you request. Please try again later");
	}

}

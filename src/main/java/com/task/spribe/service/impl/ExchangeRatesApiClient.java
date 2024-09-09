package com.task.spribe.service.impl;

import com.task.spribe.model.ExchangeRateResponse;
import com.task.spribe.service.BaseExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Implementation of the BaseExchangeRateService interface for interacting with the external exchange rates API.
 */
@Service
@RequiredArgsConstructor
public class ExchangeRatesApiClient implements BaseExchangeRateService {

    private final RestTemplate restTemplate;

    @Value("${exchangerates.api.url}")
    private String apiUrl;

    @Value("${exchangerates.api.key}")
    private String apiKey;

    /**
     * Fetches the latest exchange rates from the external API.
     *
     * @return a map of currency codes to exchange rates
     * @throws RuntimeException if the API call fails or returns an unexpected status
     */
    @Override
    public Map<String, BigDecimal> getLatestExchangeRates() {
        String url = String.format("%s?access_key=%s", apiUrl, apiKey);
        ResponseEntity<ExchangeRateResponse> response = restTemplate.getForEntity(url, ExchangeRateResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getRates();
        } else {
            // Throw a runtime exceptions with error's descriptions if the API call fails
            throw new RuntimeException("Failed to fetch exchange rates");
        }
    }
}

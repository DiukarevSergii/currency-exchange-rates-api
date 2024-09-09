package com.task.spribe.service.impl;

import com.task.spribe.model.ExchangeRateResponse;
import com.task.spribe.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ExchangeRatesApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExchangeRatesApiClient exchangeRatesApiClient;

    @BeforeEach
    void setUp() {
        // Initialize mocks before each test
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLatestExchangeRates() {
        // Mock response from API
        ExchangeRateResponse mockResponse = new ExchangeRateResponse();
        mockResponse.setRates(Map.of(TestConstants.USD, TestConstants.USD_RATE));

        // Wrap the mock response in a ResponseEntity
        ResponseEntity<ExchangeRateResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        // Configure the mock RestTemplate to return the ResponseEntity
        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponse.class))).thenReturn(responseEntity);

        // Call the method to test
        Map<String, BigDecimal> rates = exchangeRatesApiClient.getLatestExchangeRates();

        // Assert the result
        assertEquals(TestConstants.USD_RATE, rates.get(TestConstants.USD));
    }
}

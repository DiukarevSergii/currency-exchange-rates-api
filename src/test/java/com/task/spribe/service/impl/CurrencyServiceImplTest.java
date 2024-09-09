package com.task.spribe.service.impl;

import com.task.spribe.model.table.ExchangeRate;
import com.task.spribe.model.table.User;
import com.task.spribe.repository.ExchangeRateRepository;
import com.task.spribe.repository.UserRepository;
import com.task.spribe.service.BaseExchangeRateService;
import com.task.spribe.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CurrencyServiceImplTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BaseExchangeRateService exchangeRateService;

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mocking the repository response when database access is required
        when(exchangeRateRepository.findAll()).thenReturn(
                Arrays.asList(
                        ExchangeRate.builder()
                                .currencyCode(TestConstants.USD)
                                .rate(TestConstants.USD_RATE)
                                .createdByUserId(TestConstants.TEST_USER_ID)
                                .updatedByUserId(TestConstants.TEST_USER_ID)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build(),
                        ExchangeRate.builder()
                                .currencyCode(TestConstants.EUR)
                                .rate(TestConstants.EUR_RATE)
                                .createdByUserId(TestConstants.TEST_SYSTEM_USER_ID)
                                .updatedByUserId(TestConstants.TEST_USER_ID)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                )
        );

        // Mocking the repository response for user lookup by username
        when(userRepository.findByUsername("SYSTEM")).thenReturn(
                Optional.of(User.builder()
                        .id(TestConstants.TEST_SYSTEM_USER_ID)
                        .username(TestConstants.TEST_SYSTEM_USER_NAME)
                        .role(TestConstants.TEST_SYSTEM_USER_ROLE)
                        .build())
        );

        when(userRepository.findByUsername(TestConstants.TEST_USER_NAME)).thenReturn(
                Optional.of(User.builder()
                        .id(TestConstants.TEST_USER_ID)
                        .username(TestConstants.TEST_USER_NAME)
                        .role(TestConstants.TEST_USER_ROLE)
                        .build())
        );
    }


    @Test
    void testUpdateExchangeRates() {
        // Mock response from BaseExchangeRateService
        Map<String, BigDecimal> mockRates = Map.of(TestConstants.USD, TestConstants.USD_RATE);
        when(exchangeRateService.getLatestExchangeRates()).thenReturn(mockRates);

        // Call the method to test
        currencyService.updateExchangeRates();

        // Verify interaction with ExchangeRateRepository for bulk update
        verify(exchangeRateRepository, times(1)).saveAll(any(List.class));

        // Check if the rate was correctly put in the map
        assertEquals(TestConstants.USD_RATE, currencyService.getExchangeRates().get(TestConstants.USD));
    }

    @Test
    void testGetExchangeRates_WhenMapIsEmpty() {
        // When map is empty, it should load from the database (mocked in this case)
        Map<String, BigDecimal> rates = currencyService.getExchangeRates();

        // Assert that the data was correctly loaded into the map from the repository
        assertEquals(TestConstants.USD_RATE, rates.get(TestConstants.USD));
        assertEquals(TestConstants.EUR_RATE, rates.get(TestConstants.EUR));
    }

    @Test
    void testGetExchangeRates_WhenMapIsNotEmpty() {
        // Pre-populate the map
        currencyService.updateExchangeRates();

        // Call the method to test
        Map<String, BigDecimal> rates = currencyService.getExchangeRates();

        // Assert the result (should not re-load from repository, just return the map)
        assertEquals(TestConstants.USD_RATE, rates.get(TestConstants.USD));
        assertEquals(TestConstants.EUR_RATE, rates.get(TestConstants.EUR));
    }

    @Test
    void testAddCurrency() {
        // Add a new currency
        currencyService.addCurrency(TestConstants.NEW, TestConstants.NEW_RATE, TestConstants.TEST_USER_ID);

        // Verify that the currency has been added
        Map<String, BigDecimal> exchangeRates = currencyService.getExchangeRates();
        assertTrue(exchangeRates.containsKey(TestConstants.NEW));
        assertEquals(TestConstants.NEW_RATE, exchangeRates.get(TestConstants.NEW));
    }


    @Test
    void testAddMultipleCurrencies() {
        // Add multiple currencies
        currencyService.addCurrency(TestConstants.USD, TestConstants.USD_RATE, TestConstants.TEST_USER_ID);
        currencyService.addCurrency(TestConstants.EUR, TestConstants.EUR_RATE, TestConstants.TEST_USER_ID);

        // Verify that the currencies have been added
        Map<String, BigDecimal> exchangeRates = currencyService.getExchangeRates();
        assertTrue(exchangeRates.containsKey(TestConstants.USD));
        assertTrue(exchangeRates.containsKey(TestConstants.EUR));
        assertEquals(TestConstants.USD_RATE, exchangeRates.get(TestConstants.USD));
        assertEquals(TestConstants.EUR_RATE, exchangeRates.get(TestConstants.EUR));
    }
}

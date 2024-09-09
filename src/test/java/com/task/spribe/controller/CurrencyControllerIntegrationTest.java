package com.task.spribe.controller;

import com.task.spribe.model.table.ExchangeRate;
import com.task.spribe.model.table.User;
import com.task.spribe.repository.ExchangeRateRepository;
import com.task.spribe.repository.UserRepository;
import com.task.spribe.service.CurrencyService;
import com.task.spribe.util.TestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CurrencyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        // Clear existing data in the exchange_rate and user tables
        exchangeRateRepository.deleteAll();
        userRepository.deleteAll();

        // Create and save users in the database
        User systemUser = User.builder()
                .username(TestConstants.TEST_SYSTEM_USER_NAME)
                .role(TestConstants.TEST_SYSTEM_USER_ROLE)
                .build();

        User testUser = User.builder()
                .username(TestConstants.TEST_USER_NAME)
                .role(TestConstants.TEST_USER_ROLE)
                .build();


        userRepository.save(systemUser);
        userRepository.save(testUser);

        // Prepare initial data for the exchange_rate table
        ExchangeRate usdRate = ExchangeRate.builder()
                .currencyCode(TestConstants.USD)
                .rate(TestConstants.USD_RATE)
                .createdByUserId(testUser.getId()) // Reference to the created user
                .updatedByUserId(testUser.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ExchangeRate eurRate = ExchangeRate.builder()
                .currencyCode(TestConstants.EUR)
                .rate(TestConstants.EUR_RATE)
                .createdByUserId(systemUser.getId()) // Reference to the SYSTEM user
                .updatedByUserId(systemUser.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save the exchange rates with proper user references
        exchangeRateRepository.save(usdRate);
        exchangeRateRepository.save(eurRate);

        // Clear the exchangeRateMap and ensure it loads from the database
        currencyService.getExchangeRates();
    }

    @AfterEach
    public void cleanUp() {
        // Clear existing data in the exchange_rate and user tables
        exchangeRateRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testGetAllCurrencies() throws Exception {
        mockMvc.perform(get("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200 OK status
                .andExpect(jsonPath("$.USD").value(TestConstants.USD_RATE))
                .andExpect(jsonPath("$.EUR").value(TestConstants.EUR_RATE));
    }

    @Test
    public void testGetExchangeRate() throws Exception {
        mockMvc.perform(get("/api/currencies/USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(TestConstants.USD_RATE));
    }

    @Test
    void testGetExchangeRate_NotFound() throws Exception {
        mockMvc.perform(get("/api/currencies/JPY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddCurrency() throws Exception {
        User testUser = userRepository.findByUsername(TestConstants.TEST_USER_NAME).orElseThrow(
                () -> new IllegalStateException("Test user not found in the database"));

        // Add a new currency using the existing user
        mockMvc.perform(MockMvcRequestBuilders.post("/api/currencies")
                        .param("code", "AAA")
                        .param("rate", "1.23")
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk());

        // Verify that the currency has been added and can be retrieved
        mockMvc.perform(MockMvcRequestBuilders.get("/api/currencies/AAA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals("1.23", result.getResponse().getContentAsString()));

        // Additional verification (optional) to ensure the currency is in the map
        assertTrue(currencyService.getExchangeRates().containsKey("AAA"));
        assertEquals(new BigDecimal("1.23"), currencyService.getExchangeRates().get("AAA"));
    }


}

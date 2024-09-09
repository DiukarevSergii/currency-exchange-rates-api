package com.task.spribe.controller;

import com.task.spribe.service.CurrencyService;
import com.task.spribe.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrencyControllerTest {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController currencyController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();
    }

    @Test
    void testGetAllCurrencies() throws Exception {
        // Mock service response
        when(currencyService.getExchangeRates()).thenReturn(Map.of(TestConstants.USD, TestConstants.USD_RATE));

        // Perform request and validate response
        mockMvc.perform(get("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USD").value(TestConstants.USD_RATE_VALUE));
    }

    @Test
    void testGetExchangeRate() throws Exception {
        // Mock service response
        when(currencyService.getExchangeRates()).thenReturn(Map.of(TestConstants.USD, TestConstants.USD_RATE));

        // Perform request and validate response
        mockMvc.perform(get("/api/currencies/USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(TestConstants.USD_RATE_VALUE));
    }
}

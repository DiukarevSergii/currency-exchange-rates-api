package com.task.spribe.service;

import com.task.spribe.service.impl.CurrencyServiceImpl;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;

/**
 * Integration test to verify that the scheduled task for updating currency exchange rates
 * in the CurrencyServiceImpl is triggered as expected.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "scheduling.currency-update.cron=0/1 * * * * *" // Setting the cron expression for the test to 1 second interval
})
@ActiveProfiles("test")
public class CurrencyServiceSchedulingTest {

    @SpyBean
    private CurrencyServiceImpl currencyServiceImpl; // Spying on the real bean!!! https://www.youtube.com/shorts/rvGbCN1vPUM

    @Test
    void testScheduledUpdateExchangeRates() {
        // Waits up to 3 seconds for the updateExchangeRates method to be invoked
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(currencyServiceImpl).updateExchangeRates(); // Verifies that the scheduled method was called
        });
    }
}

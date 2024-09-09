package com.task.spribe.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service interface for managing currency exchange rates.
 */
public interface CurrencyService {

    /**
     * Updates exchange rates by fetching the latest data from an external API.
     * This method is scheduled to run periodically.
     */
    void updateExchangeRates();

    /**
     * Retrieves exchange rates, either from memory or from the database.
     *
     * @return a map of currency codes to exchange rates
     */
    Map<String, BigDecimal> getExchangeRates();

    /**
     * Adds a new currency's exchange rate.
     * <p>
     * This method adds a new exchange rate for the specified currency.
     * If the currency already exists, an exception is thrown to prevent overwriting.
     *
     * @param currencyCode the currency code (e.g., "USD")
     * @param rate         the exchange rate value
     * @param userId       the ID of the user performing the operation
     * @throws IllegalStateException if the currency already exists
     */
    void addCurrency(String currencyCode, BigDecimal rate, Long userId);
}

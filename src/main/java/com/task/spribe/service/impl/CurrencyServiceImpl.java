package com.task.spribe.service.impl;

import com.task.spribe.model.table.ExchangeRate;
import com.task.spribe.model.table.User;
import com.task.spribe.repository.ExchangeRateRepository;
import com.task.spribe.repository.UserRepository;
import com.task.spribe.service.BaseExchangeRateService;
import com.task.spribe.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of the CurrencyService interface for managing exchange rates.
 * It retrieves and stores exchange rates from external APIs and the database.
 */
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final UserRepository userRepository;
    private final BaseExchangeRateService exchangeRateService;

    /**
     * A thread-safe map to store the exchange rates in memory.
     * <p>
     * The choice of ConcurrentHashMap over a standard HashMap is due to the need
     * for thread safety in a concurrent environment. For example, in this application,
     * exchange rates may be updated by the scheduled task in one thread while being
     * read by a user request in another thread. If a HashMap were used, simultaneous
     * access from multiple threads could lead to unpredictable behavior, such as data
     * corruption or runtime exceptions.
     */
    private final Map<String, BigDecimal> exchangeRateMap = new ConcurrentHashMap<>();

    /**
     * Schedules the update of exchange rates by fetching the latest rates from the external API
     * and updating the corresponding records in the database. This method is executed as a scheduled
     * task according to the cron expression defined in the application properties.
     * <p>
     * The method is transactional, ensuring that all database operations within the method are
     * executed within a single transaction. If an error occurs during the update, the transaction
     * will be rolled back, and no partial updates will be applied.
     * <p>
     * The process involves:
     * 1. Fetching the latest exchange rates from the external API.
     * 2. Bulk updating the exchange rates in the database.
     * 3. Updating the in-memory map with the new rates.
     */

    @Override
    @Scheduled(cron = "${scheduling.currency-update.cron}")
    @Transactional
    public void updateExchangeRates() {
        logger.info("Updating exchange rates...");

        // Fetch the latest exchange rates from the external API
        Map<String, BigDecimal> rates = exchangeRateService.getLatestExchangeRates();

        // Prepare and perform the bulk update
        performBulkUpdate(rates);

        logger.info("Exchange rates updated.");
    }

    /**
     * Performs a bulk update of exchange rates by preparing the list of exchange rates
     * to be saved and executing the save operation in one batch.
     *
     * @param rates A map of currency codes to exchange rates fetched from the external API.
     */
    private void performBulkUpdate(Map<String, BigDecimal> rates) {
        User systemUser = getSystemUser();

        List<ExchangeRate> exchangeRatesToUpdate = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
            String currencyCode = entry.getKey();
            BigDecimal rate = entry.getValue();

            ExchangeRate exchangeRate = prepareExchangeRate(currencyCode, rate, systemUser);
            exchangeRatesToUpdate.add(exchangeRate);

            // Update the in-memory map with the new exchange rate
            exchangeRateMap.put(currencyCode, rate);
        }

        // Perform a bulk save operation
        exchangeRateRepository.saveAll(exchangeRatesToUpdate);
    }

    /**
     * Retrieves the SYSTEM user from the database. The SYSTEM user is expected to be present
     * in the database; otherwise, an IllegalStateException is thrown.
     *
     * @return The SYSTEM user entity.
     */
    private User getSystemUser() {
        return userRepository.findByUsername("SYSTEM")
                .orElseThrow(() -> new IllegalStateException("SYSTEM user not found in the database"));
    }

    /**
     * Prepares an ExchangeRate entity for either creation or update based on whether the
     * exchange rate already exists in the database.
     *
     * @param currencyCode The currency code (e.g., "USD").
     * @param rate The exchange rate value.
     * @param systemUser The SYSTEM user performing the operation.
     * @return The prepared ExchangeRate entity.
     */
    private ExchangeRate prepareExchangeRate(String currencyCode, BigDecimal rate, User systemUser) {
        ExchangeRate existingRate = exchangeRateRepository.findByCurrencyCode(currencyCode).orElse(null);

        if (existingRate == null) {
            // If no record is found, create a new one with the provided details
            return ExchangeRate.builder()
                    .currencyCode(currencyCode)
                    .rate(rate)
                    .createdAt(LocalDateTime.now())
                    .createdByUserId(systemUser.getId())
                    .updatedByUserId(systemUser.getId())
                    .build();
        } else {
            // If a record is found, update the rate and the user who modified it
            existingRate.setRate(rate);
            existingRate.setUpdatedAt(LocalDateTime.now());
            existingRate.setUpdatedByUserId(systemUser.getId());
            return existingRate;
        }
    }


    /**
     * Retrieves all exchange rates from the in-memory map. If the map is empty,
     * the rates are loaded from the database.
     *
     * @return a map of currency codes to exchange rates
     */
    @Override
    public Map<String, BigDecimal> getExchangeRates() {
        // Load exchange rates from the database if the in-memory map is empty
        if (exchangeRateMap.isEmpty()) {
            Iterable<ExchangeRate> ratesFromDb = exchangeRateRepository.findAll();
            exchangeRateMap.putAll(
                    StreamSupport.stream(ratesFromDb.spliterator(), false)
                            .collect(Collectors.toMap(ExchangeRate::getCurrencyCode, ExchangeRate::getRate))
            );
        }
        // Return a copy of the in-memory map to avoid modification by callers
        return new ConcurrentHashMap<>(exchangeRateMap);
    }

    /**
     * Adds a new currency exchange rate to the system.
     * <p>
     * This method checks if a currency with the specified code already exists in the database.
     * If the currency exists, an IllegalStateException is thrown.
     * If the currency does not exist, it is created with the provided rate and user information,
     * and then saved to the database. The in-memory map is also updated with the new rate.
     * <p>
     * The method is transactional, meaning that all operations within this method are executed
     * in a single transaction. If any operation fails, the entire transaction is rolled back.
     *
     * @param currencyCode the unique code of the currency (e.g., "USD").
     * @param rate the exchange rate for the currency.
     * @param userId the ID of the user performing the operation.
     * @throws IllegalStateException if a currency with the specified code already exists.
     */
    @Override
    @Transactional
    public void addCurrency(String currencyCode, BigDecimal rate, Long userId) {
        Optional<ExchangeRate> existingRate = exchangeRateRepository.findByCurrencyCode(currencyCode);

        if (existingRate.isPresent()) {
            throw new IllegalStateException("Currency already exists: " + currencyCode);
        }

        ExchangeRate newRate = ExchangeRate.builder()
                .currencyCode(currencyCode)
                .rate(rate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdByUserId(userId)
                .updatedByUserId(userId)
                .build();

        // Save the new rate to the database
        exchangeRateRepository.save(newRate);

        // Update the in-memory map with the new rate
        exchangeRateMap.put(currencyCode, rate);
    }

}

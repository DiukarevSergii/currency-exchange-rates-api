package com.task.spribe.repository;

import com.task.spribe.model.table.ExchangeRate;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, Long> {
    /**
     * Finds an exchange rate by its currency code.
     *
     * @param currencyCode the currency code (e.g., "USD")
     * @return an Optional containing the exchange rate if found, or an empty Optional if not
     */
    Optional<ExchangeRate> findByCurrencyCode(String currencyCode);
}

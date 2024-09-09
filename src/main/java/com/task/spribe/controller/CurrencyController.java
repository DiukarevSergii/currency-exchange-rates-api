package com.task.spribe.controller;

import com.task.spribe.exception.ResourceNotFoundException;
import com.task.spribe.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public Map<String, BigDecimal> getAllCurrencies() {
        return currencyService.getExchangeRates();
    }

    @GetMapping("/{code}")
    public BigDecimal getExchangeRate(@PathVariable String code) {
        // Attempt to get the exchange rate from the map
        BigDecimal rate = currencyService.getExchangeRates().get(code);

        // If the currency code is not found, throw a ResourceNotFoundException
        if (rate == null) {
            throw new ResourceNotFoundException("Currency not found: " + code);
        }

        return rate;
    }

    @PostMapping
    public void addCurrency(@RequestParam String code, @RequestParam BigDecimal rate, @RequestParam Long userId) {
        // In a real-world application, the userId should be extracted from the authentication token
        currencyService.addCurrency(code, rate, userId);
    }

}

package com.task.spribe.service;

import java.math.BigDecimal;
import java.util.Map;

public interface BaseExchangeRateService {
    Map<String, BigDecimal> getLatestExchangeRates();
}

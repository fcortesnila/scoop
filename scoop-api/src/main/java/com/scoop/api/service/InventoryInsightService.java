package com.scoop.api.service;

import com.scoop.api.dto.ApiDtos.InventoryAlert;
import com.scoop.api.dto.ApiDtos.InventoryResponse;
import com.scoop.api.repository.IngredientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;

@Service
public class InventoryInsightService {
    private static final Logger log = LoggerFactory.getLogger(InventoryInsightService.class);
    private final IngredientRepository ingredients;
    private final ClaudeClient claude;
    private final int serviceEndHour;
    private volatile InventoryResponse cached;

    public InventoryInsightService(IngredientRepository ingredients, ClaudeClient claude,
                                   @Value("${app.service-end-hour:22}") int serviceEndHour) {
        this.ingredients = ingredients;
        this.claude = claude;
        this.serviceEndHour = Math.max(0, Math.min(23, serviceEndHour));
    }

    @Scheduled(fixedRate = 1_800_000, initialDelay = 30_000)
    public void scheduledRefresh() {
        refresh();
    }

    public InventoryResponse getAlerts() {
        InventoryResponse snapshot = cached;
        if (snapshot == null) {
            synchronized (this) {
                if (cached == null) refresh();
                snapshot = cached;
            }
        }
        return snapshot;
    }

    private void refresh() {
        BigDecimal hoursRemaining = BigDecimal.valueOf(Math.max(0,
                serviceEndHour - LocalTime.now().getHour()));
        List<InventoryAlert> alerts = ingredients.findInventoryUsage().stream()
                .map(row -> {
                    BigDecimal usage = row.getAverageUsage() == null ? BigDecimal.ZERO : row.getAverageUsage();
                    BigDecimal hours = usage.signum() == 0 ? null
                            : row.getStock().divide(usage, 2, RoundingMode.HALF_UP);
                    boolean belowThreshold = row.getStock().compareTo(row.getThreshold()) <= 0;
                    boolean stockoutToday = hours != null && hours.compareTo(hoursRemaining) <= 0;
                    if (!belowThreshold && !stockoutToday) return null;
                    String urgency = row.getStock().signum() == 0 || (hours != null && hours.compareTo(BigDecimal.valueOf(2)) <= 0)
                            ? "CRITICAL" : belowThreshold ? "HIGH" : "MEDIUM";
                    return new InventoryAlert(row.getIngredientId(), row.getIngredientName(), row.getUnit(),
                            row.getStock(), usage.setScale(4, RoundingMode.HALF_UP), hours, row.getThreshold(), urgency);
                }).filter(alert -> alert != null).toList();
        String recommendation = null;
        if (!alerts.isEmpty()) {
            try {
                recommendation = claude.complete("Give a short plain-language restock recommendation using only these computed ingredient alerts. Do not invent quantities. Alerts: "
                        + alerts, 180).strip();
            } catch (RuntimeException exception) {
                log.warn("Claude inventory recommendation unavailable: {}", exception.getMessage());
            }
        }
        cached = new InventoryResponse(alerts, recommendation);
    }
}

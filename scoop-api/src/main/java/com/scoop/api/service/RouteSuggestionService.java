package com.scoop.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.scoop.api.dto.ApiDtos.RouteResponse;
import com.scoop.api.dto.ApiDtos.RouteSuggestion;
import com.scoop.api.repository.StopRepository;
import com.scoop.api.repository.StopRepository.RouteMetricRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class RouteSuggestionService {
    private static final Logger log = LoggerFactory.getLogger(RouteSuggestionService.class);
    private final StopRepository stops;
    private final ClaudeClient claude;

    public RouteSuggestionService(StopRepository stops, ClaudeClient claude) {
        this.stops = stops;
        this.claude = claude;
    }

    public RouteResponse suggestions() {
        List<RouteMetricRow> rows = stops.findRouteMetrics().stream().filter(row -> row.getOrderCount() != null
                && row.getOrderCount() > 0).toList();
        Map<String, Candidate> candidates = new LinkedHashMap<>();
        long totalOrders = 0;
        for (RouteMetricRow row : rows) {
            String key = row.getStopName().trim().toLowerCase(Locale.ROOT);
            Candidate candidate = candidates.computeIfAbsent(key, ignored -> new Candidate(row.getStopName(),
                    row.getLatitude(), row.getLongitude()));
            candidate.orderValue = candidate.orderValue.add(row.getOrderValue());
            candidate.orderCount += row.getOrderCount();
            if (candidate.visitedStops.add(row.getStopId())) {
                candidate.minutes += Math.max(1, row.getStopMinutes());
            }
            candidate.lastVisited = candidate.lastVisited == null || row.getLastVisited().isAfter(candidate.lastVisited)
                    ? row.getLastVisited() : candidate.lastVisited;
            candidate.hourly.add(Map.of("dayOfWeek", row.getDayOfWeek(), "hour", row.getHour(),
                    "orderValue", row.getOrderValue(), "orders", row.getOrderCount()));
            totalOrders += row.getOrderCount();
        }
        boolean enoughHistory = totalOrders >= 10;
        if (candidates.isEmpty()) return new RouteResponse(false, List.of());
        List<Candidate> ranked = candidates.values().stream()
                .sorted(Comparator.comparing(Candidate::averageHourlyRevenue).reversed()).toList();
        if (!enoughHistory) return new RouteResponse(false, fallback(ranked));

        try {
            String summary = ranked.stream().map(Candidate::summary).toList().toString();
            String response = claude.complete("""
                    Pick the top 3 stops for the next truck service window based on these aggregated stop metrics.
                    Consider day/hour patterns, order value per service hour, orders per hour, and last visit.
                    Return only strict JSON in this shape: {"suggestions":[{"stopName":"exact candidate name","reason":"one concise sentence"}]}.
                    Do not add stops absent from the data. Aggregated metrics: %s
                    """.formatted(summary), 500);
            JsonNode parsed = claude.parse(response.strip());
            JsonNode suggestions = parsed.path("suggestions");
            if (!suggestions.isArray()) throw new IllegalArgumentException("Expected a suggestions array");
            List<RouteSuggestion> validated = new ArrayList<>();
            Set<String> used = new HashSet<>();
            for (JsonNode suggestion : suggestions) {
                String name = suggestion.path("stopName").asText("").trim();
                String reason = suggestion.path("reason").asText("").trim();
                Candidate candidate = candidates.values().stream()
                        .filter(value -> value.name.equalsIgnoreCase(name)).findFirst().orElse(null);
                if (candidate == null || reason.isBlank() || reason.length() > 240
                        || !used.add(candidate.name.toLowerCase(Locale.ROOT))) continue;
                validated.add(new RouteSuggestion(candidate.name, candidate.latitude, candidate.longitude, reason));
                if (validated.size() == 3) break;
            }
            if (validated.isEmpty()) throw new IllegalArgumentException("Claude returned no valid candidate stops");
            return new RouteResponse(true, validated);
        } catch (RuntimeException | com.fasterxml.jackson.core.JsonProcessingException exception) {
            log.warn("Claude route suggestions unavailable; using historical performers: {}", exception.getMessage());
            return new RouteResponse(true, fallback(ranked));
        }
    }

    private List<RouteSuggestion> fallback(List<Candidate> candidates) {
        return candidates.stream().limit(3).map(candidate -> new RouteSuggestion(candidate.name,
                candidate.latitude, candidate.longitude, "A top historical performer at about "
                + candidate.averageHourlyRevenue().setScale(2, RoundingMode.HALF_UP) + " in orders per service hour.")).toList();
    }

    private static final class Candidate {
        private final String name;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private BigDecimal orderValue = BigDecimal.ZERO;
        private long orderCount;
        private long minutes;
        private java.time.LocalDateTime lastVisited;
        private final List<Map<String, Object>> hourly = new ArrayList<>();
        private final Set<Long> visitedStops = new HashSet<>();

        private Candidate(String name, BigDecimal latitude, BigDecimal longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        private BigDecimal averageHourlyRevenue() {
            return minutes == 0 ? BigDecimal.ZERO
                    : orderValue.multiply(BigDecimal.valueOf(60)).divide(BigDecimal.valueOf(minutes), 4, RoundingMode.HALF_UP);
        }

        private Map<String, Object> summary() {
            return Map.of("stopName", name, "averageOrderValuePerHour", averageHourlyRevenue(),
                    "ordersPerHour", minutes == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(orderCount * 60.0 / minutes),
                    "lastVisited", String.valueOf(lastVisited), "dayHourPerformance", hourly);
        }
    }
}

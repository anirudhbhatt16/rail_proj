package com.railops.health;

import com.railops.config.AiServiceConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final AiServiceConfig aiServiceConfig;

    public HealthController(JdbcTemplate jdbcTemplate,
                           RestTemplate restTemplate,
                           AiServiceConfig aiServiceConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
        this.aiServiceConfig = aiServiceConfig;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "railops-backend");
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());

        Map<String, Object> databaseHealth = checkDatabase();
        response.put("database", databaseHealth);

        Map<String, Object> aiHealth = checkAiService();
        response.put("aiService", aiHealth);

        if (!"UP".equals(databaseHealth.get("status")) || !"UP".equals(aiHealth.get("status"))) {
            response.put("status", "DEGRADED");
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("status", "UP");
            result.put("message", "Database is reachable");
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("message", "Database is unavailable");
        }
        return result;
    }

    private Map<String, Object> checkAiService() {
        Map<String, Object> result = new LinkedHashMap<>();
        String url = aiServiceConfig.getUrl();

        if (url == null || url.isBlank()) {
            result.put("status", "NOT_CONFIGURED");
            result.put("message", "AI service URL is not configured");
            return result;
        }

        try {
            String healthUrl = url.endsWith("/") ? url + "health" : url + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                result.put("status", "UP");
                result.put("message", "AI service is reachable");
                result.put("details", response.getBody());
                return result;
            }
            result.put("status", "DOWN");
            result.put("message", "AI service responded with status " + response.getStatusCodeValue());
            return result;
        } catch (RestClientException e) {
            result.put("status", "DOWN");
            result.put("message", "AI service is unavailable or timed out");
            return result;
        }
    }
}

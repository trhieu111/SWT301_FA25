package com.swp.myleague.utils.gemini_matchevent;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String API_KEY;

    @Value("${openai.api.url}")
    private String API_URL;
    
    @Autowired RestTemplate restTemplate;

    public String generate(String prompt) {
        Map<String, Object> contentMap = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contentMap, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            List<Map> candidates = (List<Map>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map first = candidates.get(0);
                Map content = (Map) first.get("content");
                List<Map> parts = (List<Map>) content.get("parts");
                return (String) parts.get(0).get("text");
            } else {
                return "⚠️ Không có phản hồi từ Gemini";
            }
        } catch (Exception e) {
            return "❌ Lỗi Gemini: " + e.getMessage();
        }
    }
}


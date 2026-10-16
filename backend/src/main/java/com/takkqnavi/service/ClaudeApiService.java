package com.takkqnavi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takkqnavi.entity.Region;
import com.takkqnavi.entity.Tournament;
import com.takkqnavi.repository.RegionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClaudeApiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RegionRepository regionRepository;

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.model}")
    private String model;

    public ClaudeApiService(
            @Value("${claude.api.url}") String apiUrl,
            ObjectMapper objectMapper,
            RegionRepository regionRepository) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
        this.objectMapper = objectMapper;
        this.regionRepository = regionRepository;
    }

    /**
     * 半年分の大会情報を取得
     */
    public List<Tournament> fetchTournaments(int months) {
        List<Tournament> allTournaments = new ArrayList<>();
        List<Region> regions = regionRepository.findAll();

        for (Region region : regions) {
            try {
                List<Tournament> tournaments = fetchTournamentsForRegion(region, months);
                allTournaments.addAll(tournaments);
                log.info("Fetched {} tournaments for {}", tournaments.size(), region.getName());
            } catch (Exception e) {
                log.error("Failed to fetch tournaments for {}: {}", region.getName(), e.getMessage());
            }
        }

        return allTournaments;
    }

    /**
     * 指定地域の大会情報を取得
     */
    public List<Tournament> fetchTournamentsForRegion(Region region, int months) {
        String prompt = buildPrompt(region.getName(), months);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 4096,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        String response = webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseResponse(response, region);
    }

    /**
     * プロンプトを生成
     */
    private String buildPrompt(String regionName, int months) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusMonths(months);

        return String.format("""
                %sで開催される卓球大会を教えてください。
                期間: %s から %s まで

                以下のJSON形式で回答してください。大会情報が見つからない場合は空配列[]を返してください。

                ```json
                [
                  {
                    "name": "大会名",
                    "category": "general または para",
                    "startDate": "YYYY-MM-DD",
                    "venue": "会場名",
                    "address": "住所",
                    "entryDeadline": "YYYY-MM-DD",
                    "summary": "大会の概要"
                  }
                ]
                ```

                注意:
                - 実在する大会情報のみを返してください
                - 日付はYYYY-MM-DD形式で返してください
                - categoryは一般の大会は"general"、パラ卓球大会は"para"としてください
                """, regionName, today, endDate);
    }

    /**
     * Claude APIのレスポンスをパース
     */
    private List<Tournament> parseResponse(String response, Region region) {
        List<Tournament> tournaments = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("content").get(0).path("text");
            String text = content.asText();

            // JSONブロックを抽出
            String jsonStr = extractJson(text);
            if (jsonStr == null || jsonStr.equals("[]")) {
                return tournaments;
            }

            List<Map<String, String>> items = objectMapper.readValue(
                    jsonStr, new TypeReference<List<Map<String, String>>>() {});

            for (Map<String, String> item : items) {
                Tournament tournament = Tournament.builder()
                        .name(item.get("name"))
                        .category(item.get("category"))
                        .startDate(parseDate(item.get("startDate")))
                        .venue(item.get("venue"))
                        .address(item.get("address"))
                        .region(region)
                        .entryDeadline(parseDate(item.get("entryDeadline")))
                        .summary(item.get("summary"))
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                tournaments.add(tournament);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Claude response: {}", e.getMessage());
        }

        return tournaments;
    }

    /**
     * テキストからJSONを抽出
     */
    private String extractJson(String text) {
        int start = text.indexOf("[");
        int end = text.lastIndexOf("]");
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    /**
     * 日付文字列をパース
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}

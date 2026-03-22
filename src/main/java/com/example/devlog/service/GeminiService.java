package com.example.devlog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${google.gemini.api.key}")
    private String apiKey;

    private final String apiUrlBase = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public record AiAnalysisResponse(String summary, String tags) {}

    public String getStudyAdvice(String learningData) {
        if (learningData == null || learningData.isEmpty()) {
            return "まだ記録がありませんね。まずは最初の一歩を記しましょう。";
        }

        // String.formatの代わりにreplaceを使用し、%記号によるエラーを回避
        String prompt = """
            あなたは専属執事兼テックリードの『アルフレッド』です。
            以下の学習記録を読み、プロの視点から短く、かつ励みになるアドバイスをください。
            一人称は「私」、ご主人様を「ご主人様」と呼ぶこと。
            
            ■学習記録:
            {DATA}
            """.replace("{DATA}", learningData);

        try {
            String response = callGeminiApi(prompt);
            return extractTextFromResponse(response);
        } catch (Exception e) {
            // 【重要】エラーをコンソールに出力する
            System.err.println("--- AIアドバイス取得中にエラーが発生しました ---");
            e.printStackTrace();
            return "申し訳ございません。現在、アドバイスを生成する準備が整わないようです。";
        }
    }

    public AiAnalysisResponse analyzeRecord(String title, String memo) {
        String prompt = """
            以下の学習内容を解析し、必ず指定のJSON形式で返してください。
            1. summary: 内容の簡潔な要約（60文字以内）
            2. tags: 技術キーワード（最大3つ、カンマ区切り）

            ■タイトル: {TITLE}
            ■メモ: {MEMO}

            回答はJSONのみを返し、余計な説明は省いてください。
            {"summary": "...", "tags": "..."}
            """
                .replace("{TITLE}", title != null ? title : "")
                .replace("{MEMO}", memo != null ? memo : "");

        try {
            String rawJson = callGeminiApi(prompt);
            String content = extractTextFromResponse(rawJson);

            String cleanedJson = content.replaceAll("```json|```", "").trim();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode result = mapper.readTree(cleanedJson);

            return new AiAnalysisResponse(
                    result.path("summary").asText("要約の生成に失敗しました"),
                    result.path("tags").asText("なし")
            );
        } catch (Exception e) {
            // 【重要】エラーをコンソールに出力する
            System.err.println("--- AI解析中にエラーが発生しました ---");
            e.printStackTrace();
            return new AiAnalysisResponse("解析エラーが発生しました", "Error");
        }
    }

    private String callGeminiApi(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("APIキーが設定されていません。");
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 2. 【重要】文字列のまま渡さず、URIオブジェクトに変換してエンコードの罠を回避します
        java.net.URI uri = java.net.URI.create(apiUrlBase + apiKey);
        return restTemplate.postForObject(uri, entity, String.class);
    }

    private String extractTextFromResponse(String responseBody) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);

        JsonNode candidates = root.path("candidates");
        // 安全な抽出：要素が存在するか確認する
        if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
            throw new RuntimeException("APIからの予期せぬレスポンス: " + responseBody);
        }

        return candidates.get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }
}
package com.example.devlog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini APIと通信し、学習データの解析やアドバイス生成を行うサービス。
 */
@Service
public class GeminiService {

    @Value("${google.gemini.api.key}")
    private String apiKey;

    // 最新の安定版モデルを使用します
    private final String apiUrlBase = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    /**
     * AIの解析結果を保持するデータ構造（Record）
     */
    public record AiAnalysisResponse(String summary, String tags) {}

    /**
     * 1. 既存機能：学習記録全体に基づいた執事のアドバイスを取得します。
     */
    public String getStudyAdvice(String learningData) {
        if (learningData == null || learningData.isEmpty()) {
            return "まだ記録がありませんね。まずは最初の一歩を記しましょう。";
        }

        String prompt = String.format("""
            あなたは専属執事兼テックリードの『アルフレッド』です。
            以下の学習記録を読み、プロの視点から短く、かつ励みになるアドバイスをください。
            一人称は「私」、ご主人様を「ご主人様」と呼ぶこと。
            
            ■学習記録:
            %s
            """, learningData);

        try {
            String response = callGeminiApi(prompt);
            return extractTextFromResponse(response);
        } catch (Exception e) {
            return "申し訳ございません。現在、アドバイスを生成する準備が整わないようです。";
        }
    }

    /**
     * 2. 新機能：特定の投稿内容を解析し、要約とタグを生成します。
     */
    public AiAnalysisResponse analyzeRecord(String title, String memo) {
        String prompt = String.format("""
            以下の学習内容を解析し、必ず指定のJSON形式で返してください。
            1. summary: 内容の簡潔な要約（60文字以内）
            2. tags: 技術キーワード（最大3つ、カンマ区切り）

            ■タイトル: %s
            ■メモ: %s

            回答はJSONのみを返し、余計な説明は省いてください。
            {"summary": "...", "tags": "..."}
            """, title, memo);

        try {
            String rawJson = callGeminiApi(prompt);
            String content = extractTextFromResponse(rawJson);

            // AIが ```json { ... } ``` のように返してきた場合のノイズ除去
            String cleanedJson = content.replaceAll("```json|```", "").trim();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode result = mapper.readTree(cleanedJson);

            return new AiAnalysisResponse(
                    result.path("summary").asText("要約の生成に失敗しました"),
                    result.path("tags").asText("なし")
            );
        } catch (Exception e) {
            return new AiAnalysisResponse("解析エラーが発生しました", "Error");
        }
    }

    /**
     * 【共通】Gemini APIへのPOSTリクエストを実行します。
     */
    private String callGeminiApi(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("APIキーが設定されていません。");
        }

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        return restTemplate.postForObject(apiUrlBase + apiKey, requestBody, String.class);
    }

    /**
     * 【共通】APIのレスポンスからテキスト部分のみを抽出します。
     */
    private String extractTextFromResponse(String responseBody) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        return root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }
}
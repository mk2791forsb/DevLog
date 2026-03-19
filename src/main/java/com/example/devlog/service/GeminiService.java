package com.example.devlog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 * Gemini APIと通信し、学習データに基づいた技術アドバイスを生成するサービス。
 */
@Service
public class GeminiService {

    @Value("${google.gemini.api.key}")
    private String apiKey;

    // APIエンドポイントのベースURL
    private final String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    /**
     * 学習記録を解析し、AI執事としての技術的助言を取得します。
     * @param learningData 整形された学習ログのテキスト
     * @return AIによるMarkdown形式のアドバイス
     */
    public String getStudyAdvice(String learningData) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "APIキーが設定されていません。application.propertiesを確認してください。";
        }

        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = baseUrl + apiKey;

        // AIへのシステム指示（キャラクター性、出力形式の定義）
        String prompt = String.format("""
            あなたは専属執事兼テックリードの『アルフレッド』です。
            一流エンジニア（プログラマー）を目指して日々研鑽を積む『ご主人様』に対し、
            提供された学習記録を分析して、技術的・精神的な助言を行ってください。
            
            ■出力の指針:
            1. 一人称は「私（わたくし）」、二人称は「ご主人様」で統一すること。
            2. 常に丁寧かつ謙虚な執事の口調でありながら、内容はプロのテックリードとして鋭く有益であること。
            3. ご主人様の学習の継続を最大限に肯定し、モチベーションを高めること。
            4. 説教や小言のような表現は避け、共感と建設的な提案を重視すること。
            
            ■出力形式:
            - Markdown形式を使用し、適切な見出し（##）、箇条書き（-）で構造化すること。
            - 文章が長くなる場合は、論点を整理して読みやすくすること。

            ■学習記録データ:
            %s
            """, learningData);

        // APIリクエストボディの構築
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));
        requestBody.put("contents", List.of(content));

        try {
            // POSTリクエストの実行
            Map<String, Object> response = restTemplate.postForObject(apiUrl, requestBody, Map.class);

            // レスポンスからテキスト部分を抽出
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> contentPart = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentPart.get("parts");

            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            // 通信失敗時のフォールバックメッセージ
            return "## 通信エラー\n申し訳ございません。AIとの回線が不安定なようでございます。しばらく経ってから再度お呼びください。";
        }
    }
}
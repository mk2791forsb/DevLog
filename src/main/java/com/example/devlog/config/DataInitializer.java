package com.example.devlog.config;

import com.example.devlog.model.StudyRecord;
import com.example.devlog.repository.StudyRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * アプリケーション起動時に初期データを投入する設定クラスです。
 * 開発時の動作確認用に、起動ごとにテストデータを作成します。
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(StudyRecordRepository repository) {
        return args -> {
            // 起動時に1件のサンプルデータを作成してデータベースに保存
            StudyRecord record = new StudyRecord();
            record.setTitle("Java/Spring Boot 開発環境構築");
            record.setDuration(120);
            record.setMemo("Spring Initializrを利用してプロジェクトを作成し、起動確認を完了。");

            repository.save(record);

            // ログ出力（実際の開発現場ではSLF4JなどのLoggerを使用することが推奨されます）
            System.out.println("--- 初期データの投入が完了しました ---");
        };
    }
}
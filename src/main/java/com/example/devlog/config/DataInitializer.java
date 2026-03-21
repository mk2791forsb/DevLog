package com.example.devlog.config;

import com.example.devlog.model.StudyRecord;
import com.example.devlog.repository.StudyRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * アプリケーション起動時の初期化設定。
 * データベースが空の場合のみ、最初の一歩を記録します。
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(StudyRecordRepository repository) {
        return args -> {
            // 1. データベースが空かどうかを確認（プロの定石です）
            if (repository.count() == 0) {
                StudyRecord record = new StudyRecord();
                // ご主人様のご指示通り、内容を更新
                record.setTitle("学習スタート");
                record.setDuration(1);
                record.setMemo("DevLogへようこそ。ここからご主人様のエンジニアとしての歩みが始まります。");

                repository.save(record);
                System.out.println("--- 初回起動：初期データを投入しました ---");
            } else {
                // 既に記録がある場合は何もしない
                System.out.println("--- 既存の記録を確認：初期データの投入をスキップします ---");
            }
        };
    }
}
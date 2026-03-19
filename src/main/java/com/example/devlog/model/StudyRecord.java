package com.example.devlog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学習記録を管理するドメインモデルです。
 * バリデーション制約を定義し、データの整合性を担保します。
 */
@Entity
@Data
public class StudyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 学習内容のタイトル。空文字は許可しません。
     */
    @NotBlank(message = "学習タイトルを入力してください。")
    private String title;

    /**
     * 学習時間（分）。1分から24時間（1440分）の範囲で制限します。
     */
    @NotNull(message = "学習時間を入力してください。")
    @Min(value = 1, message = "1分以上の学習を記録してください。")
    @Max(value = 1440, message = "24時間を超える記録はできません。")
    private Integer duration;

    /**
     * 学習の詳細や気づきを保存する任意入力のフィールドです。
     */
    private String memo;

    /**
     * レコードの作成日時。インスタンス生成時に現在時刻をセットします。
     */
    private LocalDateTime createdAt = LocalDateTime.now();
}
package com.example.devlog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class StudyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "学習タイトルを入力してください。")
    private String title;

    @NotNull(message = "学習時間を入力してください。")
    @Min(value = 1, message = "1分以上の学習を記録してください。")
    @Max(value = 1440, message = "24時間を超える記録はできません。")
    private Integer duration;

    private String memo;

    /** AIによる学習内容の要約 (追加) */
    @Column(length = 500)
    private String aiSummary;

    /** AIが抽出した技術タグ (追加) */
    private String aiTags;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
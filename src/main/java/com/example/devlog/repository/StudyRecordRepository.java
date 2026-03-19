package com.example.devlog.repository;

import com.example.devlog.model.StudyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 学習記録テーブルへのデータアクセスを制御するリポジトリインターフェースです。
 */
@Repository
public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    /**
     * タイトル、またはメモに指定キーワードを含むレコードを検索します。
     * * @param title 検索対象のタイトル
     * @param memo  検索対象のメモ
     * @return 検索条件に合致するレコードのリスト（作成日時の降順）
     */
    List<StudyRecord> findByTitleContainingOrMemoContainingOrderByCreatedAtDesc(String title, String memo);
}
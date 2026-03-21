package com.example.devlog.service;

import com.example.devlog.model.StudyRecord;
import com.example.devlog.repository.StudyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyRecordService {

    private final StudyRecordRepository repository;
    private final GeminiService geminiService;

    public StudyRecordService(StudyRecordRepository repository, GeminiService geminiService) {
        this.repository = repository;
        this.geminiService = geminiService;
    }

    @Transactional
    public void saveWithAiAnalysis(StudyRecord record) {
        // AI解析の実行
        GeminiService.AiAnalysisResponse analysis = geminiService.analyzeRecord(record.getTitle(), record.getMemo());

        // 結果をセット
        record.setAiSummary(analysis.summary());
        record.setAiTags(analysis.tags());

        // 保存
        repository.save(record);
    }
}
package com.example.devlog.controller;

import com.example.devlog.model.StudyRecord;
import com.example.devlog.repository.StudyRecordRepository;
import com.example.devlog.service.GeminiService;
import com.example.devlog.service.StudyRecordService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class StudyRecordController {

    private final StudyRecordRepository repository;
    private final GeminiService geminiService;
    private final StudyRecordService studyRecordService;

    public StudyRecordController(StudyRecordRepository repository,
                                 GeminiService geminiService,
                                 StudyRecordService studyRecordService) {
        this.repository = repository;
        this.geminiService = geminiService;
        this.studyRecordService = studyRecordService;
    }

    /**
     * ダッシュボード表示
     */
    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword, Model model) {
        List<StudyRecord> records;
        if (keyword != null && !keyword.isEmpty()) {
            records = repository.findByTitleContainingOrMemoContainingOrderByCreatedAtDesc(keyword, keyword);
        } else {
            records = repository.findAll();
        }

        // AIアドバイスの準備
        String logs = records.stream()
                .map(r -> String.format("タイトル:%s, 内容:%s", r.getTitle(), r.getMemo()))
                .collect(Collectors.joining("\n"));
        model.addAttribute("aiAdvice", geminiService.getStudyAdvice(logs));

        prepareDashboardModel(model, records);
        model.addAttribute("newRecord", new StudyRecord());
        return "index";
    }

    /**
     * 新規保存（AI解析付き）
     */
    @PostMapping("/")
    public String saveRecord(@Valid @ModelAttribute("newRecord") StudyRecord record,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            prepareDashboardModel(model, repository.findAll());
            return "index";
        }
        studyRecordService.saveWithAiAnalysis(record);
        return "redirect:/";
    }

    /**
     * 削除処理
     */
    @PostMapping("/delete/{id}")
    public String deleteRecord(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }

    /**
     * 編集画面の表示
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        StudyRecord record = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid record Id:" + id));
        model.addAttribute("record", record);
        return "edit"; // edit.html を呼び出す
    }

    /**
     * 編集の実行（更新保存）
     * ※編集時にAI要約を更新するかは要検討ですが、まずはシンプルに上書き保存します
     */
    @PostMapping("/update")
    public String updateRecord(@Valid @ModelAttribute("record") StudyRecord record,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "edit";
        }
        // 作成日時などは保持したまま更新するのが一般的です
        repository.save(record);
        return "redirect:/";
    }

    /**
     * 共通モデルの準備
     */
    private void prepareDashboardModel(Model model, List<StudyRecord> records) {
        model.addAttribute("records", records);
        List<String> labels = records.stream()
                .map(r -> r.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd")))
                .collect(Collectors.toList());
        List<Integer> data = records.stream()
                .map(StudyRecord::getDuration)
                .collect(Collectors.toList());
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", data);
    }
}
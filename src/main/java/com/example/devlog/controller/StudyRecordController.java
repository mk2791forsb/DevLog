package com.example.devlog.controller;

import com.example.devlog.model.StudyRecord;
import com.example.devlog.repository.StudyRecordRepository;
import com.example.devlog.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学習記録のCRUD操作、検索、AI連携、およびダッシュボード表示を制御するコントローラー。
 */
@Controller
public class StudyRecordController {

    private final StudyRecordRepository repository;
    private final GeminiService geminiService;

    public StudyRecordController(StudyRecordRepository repository, GeminiService geminiService) {
        this.repository = repository;
        this.geminiService = geminiService;
    }

    /**
     * メインダッシュボード（一覧・検索・統計）の表示。
     * @param keyword 検索キーワード（任意）
     */
    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword, Model model) {
        List<StudyRecord> records;

        // 検索キーワードの有無によって取得データを切り替え
        if (keyword != null && !keyword.isEmpty()) {
            records = repository.findByTitleContainingOrMemoContainingOrderByCreatedAtDesc(keyword, keyword);
            model.addAttribute("keyword", keyword);
        } else {
            records = repository.findAll();
        }

        // フォーム用の空オブジェクトとダッシュボード用データをセット
        model.addAttribute("newRecord", new StudyRecord());
        prepareDashboardModel(model, records);

        return "index";
    }

    /**
     * 新規学習記録の保存。バリデーションエラー時は入力を保持してダッシュボードを再描画。
     */
    @PostMapping("/add")
    public String addRecord(@Valid @ModelAttribute("newRecord") StudyRecord studyRecord,
                            BindingResult result, Model model) {
        if (result.hasErrors()) {
            // エラー時もグラフやリストを表示させるためにデータを再準備
            prepareDashboardModel(model, repository.findAll());
            return "index";
        }
        repository.save(studyRecord);
        return "redirect:/";
    }

    /**
     * 削除処理。
     */
    @GetMapping("/delete/{id}")
    public String deleteRecord(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }

    /**
     * AI執事による学習状況の分析とアドバイス取得。
     */
    @GetMapping("/advice")
    public String getAdvice(Model model) {
        // 全記録をAI解析用のテキスト形式に整形
        String logs = repository.findAll().stream()
                .map(r -> String.format("[%s] %s (%d分): %s",
                        r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                        r.getTitle(), r.getDuration(), r.getMemo()))
                .collect(Collectors.joining("\n"));

        // AIサービスからアドバイスを取得
        String advice = geminiService.getStudyAdvice(logs);
        model.addAttribute("aiAdvice", advice);

        // ダッシュボード表示に必要なデータを揃えて返却
        prepareDashboardModel(model, repository.findAll());
        model.addAttribute("newRecord", new StudyRecord());
        return "index";
    }

    /**
     * 画面表示に必要な共通データ（リスト、グラフ用ラベル・数値、ヒートマップ用集計）をModelにセット。
     */
    private void prepareDashboardModel(Model model, List<StudyRecord> records) {
        model.addAttribute("records", records);

        // 折れ線グラフ用：日付と学習時間のリスト
        List<String> labels = records.stream()
                .map(r -> r.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd")))
                .collect(Collectors.toList());
        List<Integer> data = records.stream()
                .map(StudyRecord::getDuration)
                .collect(Collectors.toList());
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", data);

        // ヒートマップ用：日付ごとの学習回数を集計（Map形式）
        Map<String, Long> activityData = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                ));
        model.addAttribute("activityData", activityData);
    }
}
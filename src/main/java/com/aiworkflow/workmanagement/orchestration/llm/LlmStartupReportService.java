package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.service.LlmSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Writes LLM startup report on application startup.
 */
@Component
public class LlmStartupReportService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(LlmStartupReportService.class);
    private static final Path REPORT_PATH = Path.of("logs", "llm-startup-report.md");

    private final LlmSummaryService summaryService;
    private final LlmStartupReportWriter writer;

    public LlmStartupReportService(LlmSummaryService summaryService) {
        this.summaryService = summaryService;
        this.writer = new LlmStartupReportWriter();
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            writer.writeReport(REPORT_PATH, summaryService.getSummary());
        } catch (Exception e) {
            logger.warn("Failed to write LLM startup report: {}", e.getMessage());
        }
    }
}


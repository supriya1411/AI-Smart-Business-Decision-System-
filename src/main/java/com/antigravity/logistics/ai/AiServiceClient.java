package com.antigravity.logistics.ai;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AiServiceClient — OpenAI-powered intelligence layer
 * ─────────────────────────────────────────────────────
 * Replaces the Python FastAPI stub with direct OpenAI GPT calls.
 *
 * Endpoints powered by OpenAI:
 *   - getSentiment()         → GPT analyzes customer review text (score 1.0–5.0)
 *   - getDemandForecast()    → GPT predicts next-period demand from sales history
 *   - getBusinessDecision()  → GPT provides a natural-language business recommendation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final OpenAIClient openAIClient;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max-tokens:256}")
    private int maxTokens;

    // ─────────────────────────────────────────────────────
    // 1. SENTIMENT ANALYSIS
    // ─────────────────────────────────────────────────────

    /**
     * Analyzes a customer review using GPT and returns a sentiment score between 1.0 and 5.0.
     *
     * @param text      the review text to analyze
     * @param productId used for logging only
     * @return sentiment score (1.0 = very negative, 5.0 = very positive), or null on failure
     */
    public Double getSentiment(String text, Long productId) {
        String prompt = """
                You are a sentiment analysis engine for an e-commerce platform.
                Analyze the following customer review and return ONLY a single decimal number
                between 1.0 (very negative) and 5.0 (very positive) representing the sentiment score.
                Do not include any explanation or extra text — just the number.
                
                Review: "%s"
                """.formatted(text);

        try {
            String raw = callGpt(prompt);
            double score = Double.parseDouble(raw.trim());
            score = Math.max(1.0, Math.min(5.0, score)); // clamp
            log.info("✅ OpenAI Sentiment for product {}: {} → score={}", productId, truncate(text), score);
            return score;
        } catch (NumberFormatException e) {
            log.warn("⚠️  Could not parse sentiment score from GPT response for product {}: {}", productId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("❌ OpenAI Sentiment call failed for product {}: {}", productId, e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────
    // 2. DEMAND FORECASTING
    // ─────────────────────────────────────────────────────

    /**
     * Predicts demand for the next period using GPT based on recent sales history.
     *
     * @param productId used for logging
     * @param history   list of recent weekly sales quantities (oldest first)
     * @return forecasted units for next period, or null on failure
     */
    public Integer getDemandForecast(Long productId, List<Integer> history) {
        String historyStr = history.isEmpty() ? "no history available" : history.toString();

        String prompt = """
                You are a demand forecasting model for a retail logistics system.
                Given the following weekly sales quantities (oldest to most recent):
                %s
                
                Predict the demand (integer units) for the NEXT week.
                Consider trends and seasonality. Return ONLY a single integer number — no explanation.
                """.formatted(historyStr);

        try {
            String raw = callGpt(prompt);
            int forecast = Integer.parseInt(raw.trim());
            forecast = Math.max(0, forecast); // ensure non-negative
            log.info("✅ OpenAI Demand Forecast for product {}: history={} → forecast={}", productId, historyStr, forecast);
            return forecast;
        } catch (NumberFormatException e) {
            log.warn("⚠️  Could not parse forecast from GPT for product {}: {}", productId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("❌ OpenAI Demand Forecast call failed for product {}: {}", productId, e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────
    // 3. BUSINESS DECISION EXPLANATION (Bonus)
    // ─────────────────────────────────────────────────────

    /**
     * Asks GPT for a human-readable business recommendation based on key metrics.
     *
     * @param productName   name of the product
     * @param currentStock  current inventory count
     * @param threshold     restock threshold
     * @param trend         demand trend (RISING / STABLE / DECLINING)
     * @param avgSentiment  average customer sentiment score
     * @return natural-language recommendation string
     */
    public String getBusinessDecisionExplanation(String productName, int currentStock,
                                                  int threshold, String trend, Double avgSentiment) {
        String prompt = """
                You are an AI business advisor for a smart logistics company.
                Given the following product metrics, provide a concise, actionable business recommendation (2-3 sentences max):
                
                Product       : %s
                Current Stock : %d units
                Restock Threshold: %d units
                Demand Trend  : %s
                Avg Sentiment : %s / 5.0
                
                What should the business manager do? Be specific and brief.
                """.formatted(
                productName,
                currentStock,
                threshold,
                trend,
                avgSentiment != null ? String.format("%.1f", avgSentiment) : "N/A"
        );

        try {
            String advice = callGpt(prompt);
            log.info("✅ OpenAI Business Decision for '{}': {}", productName, truncate(advice));
            return advice.trim();
        } catch (Exception e) {
            log.error("❌ OpenAI Business Decision call failed for '{}': {}", productName, e.getMessage());
            return "Unable to generate AI recommendation at this time.";
        }
    }

    // ─────────────────────────────────────────────────────
    // Internal helper — calls OpenAI Chat Completions API
    // ─────────────────────────────────────────────────────

    private String callGpt(String userPrompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.of(model))
                .maxCompletionTokens(maxTokens)
                .addMessage(ChatCompletionUserMessageParam.builder()
                        .content(userPrompt)
                        .build())
                .build();

        ChatCompletion completion = openAIClient.chat().completions().create(params);
        return completion.choices().get(0).message().content().orElse("").trim();
    }

    private String truncate(String s) {
        return s != null && s.length() > 60 ? s.substring(0, 60) + "..." : s;
    }

    // ─────────────────────────────────────────────────────
    // Legacy record types (kept for backward compatibility)
    // ─────────────────────────────────────────────────────
    public record SentimentRequest(String text, Long product_id) {}
    public record SentimentResponse(Double score, String label) {}
    public record ForecastRequest(Long product_id, List<Integer> history) {}
    public record ForecastResponse(Integer forecast, Double confidence) {}
}

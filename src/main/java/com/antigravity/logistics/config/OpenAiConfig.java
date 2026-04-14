package com.antigravity.logistics.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI Client Configuration
 * -------------------------------------------------
 * Reads the API key from the environment variable OPENAI_API_KEY
 * (configured via application.yml: openai.api-key).
 *
 * HOW TO SET YOUR KEY:
 *   Windows PowerShell  : $env:OPENAI_API_KEY = "sk-..."
 *   Windows CMD         : set OPENAI_API_KEY=sk-...
 *   application.yml     : openai.api-key: sk-...   (NOT recommended for production)
 */
@Slf4j
@Configuration
public class OpenAiConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAIClient openAIClient() {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("your-openai")) {
            log.warn("⚠️  OpenAI API key is not set! Set OPENAI_API_KEY environment variable.");
        } else {
            log.info("✅ OpenAI client initialized successfully.");
        }
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}

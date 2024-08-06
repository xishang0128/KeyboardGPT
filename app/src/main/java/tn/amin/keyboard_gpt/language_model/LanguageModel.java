package tn.amin.keyboard_gpt.language_model;

import java.util.HashMap;
import java.util.Map;

public enum LanguageModel {
    Gemini("Gemini", "gemini-1.5-flash", "Not configurable"),
    ChatGPT("ChatGPT", "gpt-4o-mini", "https://api.openai.com"),
    Groq("Groq", "llama3-8b-8192", "https://api.groq.com/openai"),
    Claude("Claude", "claude-3-5-sonnet-20240620", "https://api.anthropic.com"),
    ;

    public final String label;
    public final Map<LanguageModelParameter, String> defaultParameters = new HashMap<>();

    LanguageModel(String label, String defaultSubModel, String defaultBaseUrl) {
        this.label = label;
        defaultParameters.put(LanguageModelParameter.SubModel, defaultSubModel);
        defaultParameters.put(LanguageModelParameter.BaseUrl, defaultBaseUrl);
    }
}

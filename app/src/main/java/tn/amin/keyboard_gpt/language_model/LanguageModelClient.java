package tn.amin.keyboard_gpt.language_model;

import androidx.annotation.NonNull;

import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

public abstract class LanguageModelClient {
    private final Map<LanguageModelParameter, String> mParameters = new HashMap<>();

    abstract public Publisher<String> submitPrompt(String prompt, String systemMessage);

    abstract public LanguageModel getLanguageModel();

    public Map<LanguageModelParameter, String> getParameters() {
        return mParameters;
    }

    public void updateParameters(Map<LanguageModelParameter, String> parameters) {
        mParameters.putAll(parameters);
    }

    public String getApiKey() {
        return getParameterInternal(LanguageModelParameter.APIKey);
    }

    public void setApiKey(String apiKey) {
        setParameterInternal(LanguageModelParameter.APIKey, apiKey);
    }

    public String getSubModel() {
        return getParameterInternal(LanguageModelParameter.SubModel);
    }

    public void setSubModel(String subModel) {
        setParameterInternal(LanguageModelParameter.SubModel, subModel);
    }

    public String getBaseUrl() {
        return getParameterInternal(LanguageModelParameter.BaseUrl);
    }

    public void setBaseUrl(String baseUrl) {
        setParameterInternal(LanguageModelParameter.SubModel, baseUrl);
    }

    public static LanguageModelClient forModel(LanguageModel model) {
        switch (model) {
            case Gemini:
                return new GeminiClient();
            case Groq:
                return new GroqClient();
            case Claude:
                return new ClaudeClient();
            case ChatGPT:
            default:
                return new ChatGPTClient();
        }
    }

    static Publisher<String> MISSING_API_KEY_PUBLISHER = subscriber -> {
        subscriber.onNext("Missing API Key");
        subscriber.onComplete();
    };

    protected static String getDefaultSystemMessage() {
        return "You are a helpful assitant.";
    }

    private String getDefaultParameter(LanguageModelParameter parameter) {
        return getLanguageModel().defaultParameters.get(parameter);
    }

    private String getParameterInternal(LanguageModelParameter parameter) {
        return mParameters.getOrDefault(parameter, getDefaultParameter(parameter));
    }

    private void setParameterInternal(LanguageModelParameter parameter, String value) {
        mParameters.put(parameter, value);
    }

    @NonNull
    @Override
    public String toString() {
        return getLanguageModel().label + " (" + getSubModel() + ")";
    }
}

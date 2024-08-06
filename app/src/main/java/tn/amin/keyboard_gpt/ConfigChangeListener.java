package tn.amin.keyboard_gpt;

import java.util.Map;

import tn.amin.keyboard_gpt.language_model.LanguageModel;
import tn.amin.keyboard_gpt.language_model.LanguageModelParameter;

public interface ConfigChangeListener {
    void onLanguageModelChange(LanguageModel model);

    void onParametersChange(LanguageModel model, Map<LanguageModelParameter, String> parameters);

    void onCommandsChange(String commandsRaw);
}

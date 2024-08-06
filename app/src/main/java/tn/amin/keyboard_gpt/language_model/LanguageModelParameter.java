package tn.amin.keyboard_gpt.language_model;

import android.text.InputType;

public enum LanguageModelParameter {
    APIKey(true, "API Key", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD),
    SubModel(false, "Model"),
    BaseUrl(false, "Base Url"),
    ;

    public final boolean required;
    public final String label;
    public final int inputType;

    LanguageModelParameter(boolean required, String label) {
        this(required, label, InputType.TYPE_CLASS_TEXT);
    }

    LanguageModelParameter(boolean required, String label, int inputType) {
        this.required = required;
        this.label = label;
        this.inputType = inputType;
    }
}

package tn.amin.keyboard_gpt;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import tn.amin.keyboard_gpt.language_model.LanguageModel;
import tn.amin.keyboard_gpt.language_model.LanguageModelParameter;

public class SPManagerCompat extends SPManager {
    protected static final String PREF_LANGUAGE_MODEL_COMPAT = "language_model";

    protected static final String PREF_API_KEY_COMPAT = "%s.api_key";

    protected static final String PREF_SUB_MODEL_COMPAT = "%s.sub_model";

    protected static final String PREF_BASE_URL_COMPAT = "%s.base_url";

    public SPManagerCompat(Context context) {
        super(context);
    }

    @Override
    public boolean hasLanguageModel() {
        return super.hasLanguageModel() || mSP.contains(PREF_LANGUAGE_MODEL_COMPAT);
    }

    @Override
    public LanguageModel getLanguageModel() {
        if (mSP.contains(PREF_LANGUAGE_MODEL_COMPAT)) {
            int index = mSP.getInt(PREF_LANGUAGE_MODEL_COMPAT, 0);
            String languageModelName = new String[] {
                    "Gemini",
                    "ChatGPT",
                    "Groq",
            } [index];
            LanguageModel languageModel = LanguageModel.valueOf(languageModelName);
            MainHook.log("Migrating legacy languageModel key \"" + PREF_LANGUAGE_MODEL_COMPAT + "\" with value \"" + index + "\"");

            mSP.edit().remove(PREF_LANGUAGE_MODEL_COMPAT).apply();
            setLanguageModel(languageModel);
            return languageModel;
        }

        return super.getLanguageModel();
    }

    @Override
    public Map<LanguageModelParameter, String> getLanguageModelParameters(LanguageModel model) {
        Map<LanguageModelParameter, String> result = super.getLanguageModelParameters(model);
        if (result.isEmpty()) {
            String apiKeyCompatKey = String.format(PREF_API_KEY_COMPAT, model.name());
            String subModelCompatKey = String.format(PREF_SUB_MODEL_COMPAT, model.name());
            String baseUrlCompatKey = String.format(PREF_BASE_URL_COMPAT, model.name());

            if (migrateParameterCompat(result, LanguageModelParameter.APIKey, apiKeyCompatKey)
                    | migrateParameterCompat(result, LanguageModelParameter.SubModel, subModelCompatKey)
                    | migrateParameterCompat(result, LanguageModelParameter.BaseUrl, baseUrlCompatKey) ) {
                result = new HashMap<>();
                MainHook.log("Migrated old LanguageModelParameter format");
                setLanguageModelParameters(model, result);
            }
        }
        return result;
    }

    private boolean migrateParameterCompat(Map<LanguageModelParameter, String> resultMap,
                                           LanguageModelParameter parameter, String keyCompat) {
        if (mSP.contains(keyCompat)) {
            String valueCompat = mSP.getString(keyCompat, null);
            if (valueCompat != null) {
                resultMap.put(parameter, valueCompat);
            }
            mSP.edit().remove(keyCompat).apply();
            return true;
        }
        return false;
    }
}

package tn.amin.keyboard_gpt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tn.amin.keyboard_gpt.instruction.command.Commands;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.language_model.LanguageModel;
import tn.amin.keyboard_gpt.language_model.LanguageModelParameter;

public class SPManager implements ConfigInfoProvider {
    protected static final String PREF_NAME = "keyboard_gpt";

    protected static final String PREF_LANGUAGE_MODEL = "language_model_v2";

    protected static final String PREF_GEN_AI_COMMANDS = "gen_ai_commands";

    protected static final String PREF_LANGUAGE_MODEL_PARAMETER = "%s.parameters";

    protected final SharedPreferences mSP;

    public SPManager(Context context) {
        mSP = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean hasLanguageModel() {
        return mSP.contains(PREF_LANGUAGE_MODEL);
    }

    @Override
    public LanguageModel getLanguageModel() {
        String languageModelName = mSP.getString(PREF_LANGUAGE_MODEL, null);
        if (languageModelName == null) {
            languageModelName = LanguageModel.Gemini.name();
        }
        return LanguageModel.valueOf(languageModelName);
    }

    public void setLanguageModel(LanguageModel model) {
        mSP.edit().putString(PREF_LANGUAGE_MODEL, model.name()).apply();
    }

    public void setLanguageModelParameters(LanguageModel model, Map<LanguageModelParameter, String> parameters) {
        String key = String.format(PREF_LANGUAGE_MODEL_PARAMETER, model.name());
        JSONObject jsonObject = new JSONObject();
        parameters.forEach((p, v) -> {
            try {
                jsonObject.put(p.name(), v);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        mSP.edit().putString(key, jsonObject.toString()).apply();
    }

    public Map<LanguageModelParameter, String> getLanguageModelParameters(LanguageModel model) {
        String key = String.format(PREF_LANGUAGE_MODEL_PARAMETER, model.name());
        String rawJson = mSP.getString(key, null);
        if (rawJson == null) {
            return Collections.emptyMap();
        }
        try {
            JSONObject jsonObject = new JSONObject(rawJson);
            Map<LanguageModelParameter, String> parameters = new HashMap<>();
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String parameterName = it.next();
                LanguageModelParameter parameter = LanguageModelParameter.valueOf(parameterName);
                parameters.put(parameter, jsonObject.getString(parameterName));
            }
            return parameters;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setGenerativeAICommandsRaw(String commands) {
        mSP.edit().putString(PREF_GEN_AI_COMMANDS, commands).apply();
    }

    public String getGenerativeAICommandsRaw() {
        return mSP.getString(PREF_GEN_AI_COMMANDS, "[]");
    }

    public void setGenerativeAICommands(List<GenerativeAICommand> commands) {
        mSP.edit().putString(PREF_GEN_AI_COMMANDS, Commands.encodeCommands(commands)).apply();
    }

    public List<GenerativeAICommand> getGenerativeAICommands() {
        return Commands.decodeCommands(mSP.getString(PREF_GEN_AI_COMMANDS, "[]"));
    }

    @Override
    public Bundle getConfigBundle() {
        Bundle bundle = new Bundle();
        for (LanguageModel model: LanguageModel.values()) {
            Bundle configBundle = new Bundle();
            Map<LanguageModelParameter, String> parameters = getLanguageModelParameters(model);
            parameters.forEach((p, v) -> configBundle.putString(p.name(), v));

            bundle.putBundle(model.name(), configBundle);
        }
        return bundle;
    }
}

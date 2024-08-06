package tn.amin.keyboard_gpt;

import android.text.InputType;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import tn.amin.keyboard_gpt.instruction.InstructionCategory;
import tn.amin.keyboard_gpt.language_model.LanguageModel;
import tn.amin.keyboard_gpt.language_model.LanguageModelClient;
import tn.amin.keyboard_gpt.language_model.LanguageModelParameter;

public class GenerativeAIController implements ConfigChangeListener {
    private LanguageModelClient mModelClient = null;

    private final SPManager mSPManager;
    private final UiInteracter mInteracter;

    public GenerativeAIController(SPManager spManager, UiInteracter interacter) {
        mSPManager = spManager;
        mInteracter = interacter;

        mInteracter.registerConfigChangeListener(this);
        if (mSPManager.hasLanguageModel()) {
            setModel(mSPManager.getLanguageModel());
        }
    }

    public boolean needModelClient() {
        return mModelClient == null;
    }

    public boolean needApiKey() {
        return mModelClient.getApiKey() == null || mModelClient.getApiKey().isEmpty();
    }

    private void setModel(LanguageModel model) {
        mModelClient = LanguageModelClient.forModel(model);
        mModelClient.updateParameters(mSPManager.getLanguageModelParameters(model));
    }

    @Override
    public void onLanguageModelChange(LanguageModel model) {
        mSPManager.setLanguageModel(model);

        if (mModelClient == null || mModelClient.getLanguageModel() != model) {
            setModel(model);
        }
    }

    @Override
    public void onParametersChange(LanguageModel model, Map<LanguageModelParameter, String> parameters) {
        mSPManager.setLanguageModelParameters(model, parameters);
        if (mModelClient != null && mModelClient.getLanguageModel() == model) {
            mModelClient.updateParameters(parameters);
        }
    }

    @Override
    public void onCommandsChange(String commandsRaw) {
        mSPManager.setGenerativeAICommandsRaw(commandsRaw);
    }

    public void generateResponse(String prompt) {
        generateResponse(prompt, null);
    }

    public void generateResponse(String prompt, String systemMessage) {
        MainHook.log("Getting response for text \"" + prompt + "\"");

        if (prompt.isEmpty()) {
            return;
        }

        if (!mInteracter.requestEditTextOwnership(InstructionCategory.Prompt)) {
            return;
        }

        mInteracter.post(() -> {
            mInteracter.setText("Generating Response...");
            mInteracter.setInputType(InputType.TYPE_NULL);
        });

        Publisher<String> publisher = mModelClient.submitPrompt(prompt, systemMessage);

        publisher.subscribe(new Subscriber<String>() {
            boolean completed = false;

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                if (s.isEmpty()) {
                    return;
                }

                MainHook.log("onNext: \"" + s + "\"");

                mInteracter.post(() -> mInteracter.getInputConnection().commitText(s, 1));
            }

            @Override
            public void onError(Throwable t) {
                XposedBridge.log(t);

                mInteracter.post(() -> {
                    mInteracter.toastLong(t.getClass().getSimpleName() + " : " + t.getMessage() + " (see logs)");
                });

                onComplete();
            }

            @Override
            public void onComplete() {
                if (completed) {
                    MainHook.log("Skipping duplicate onComplete");
                    return;
                }
                completed = true;

                mInteracter.post(() -> {
                    mInteracter.setInputType(InputType.TYPE_CLASS_TEXT);
                    mInteracter.setText("? ");
                });
                mInteracter.releaseEditTextOwnership(InstructionCategory.Prompt);
                MainHook.log("Done");
            }
        });
    }

    public LanguageModel getLanguageModel() {
        return mModelClient.getLanguageModel();
    }

    public LanguageModelClient getModelClient() {
        return mModelClient;
    }
}

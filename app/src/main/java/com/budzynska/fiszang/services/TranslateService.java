package com.budzynska.fiszang.services;

import android.os.AsyncTask;
import android.os.Handler;

import com.google.api.services.vision.v1.Vision;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TranslateService {

    private Translate translate;
    private TranslateOptions options;
    private Translation translation;
    private Handler handler;
    private String translatedText;
    private String key;

    public TranslateService(String key) {
        this.key = key;
        handler = new Handler();
        options = TranslateOptions.newBuilder().setApiKey(key).build();
        translate = options.getService();
    }

    public String getTranslatedText() {

        return translatedText;
    }

    public void translate(String textToTranslate) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                translation = translate.translate(textToTranslate, Translate.TranslateOption.sourceLanguage("en"), Translate.TranslateOption.targetLanguage("pl"));
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        translatedText = translation.getTranslatedText();
                    }
                });
                return null;
            }
        }.execute();

    }
}

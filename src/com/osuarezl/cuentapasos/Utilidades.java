package com.osuarezl.cuentapasos;

import java.util.Locale;
import android.app.Service;
import android.speech.tts.TextToSpeech;
import android.text.format.Time;
import android.util.Log;

public class Utilidades implements TextToSpeech.OnInitListener {
	private static final String TAG = "Utils";
    private Service servicio;

    private static Utilidades instancia = null;

    private Utilidades() {
    }
     
    public static Utilidades getInstance() {
        if (instancia == null) {
            instancia = new Utilidades();
        }
        return instancia;
    }
    
    public void serServicio(Service servicio) {
        this.servicio = servicio;
    }
    
    /********** SPEAKING **********/
    
    private TextToSpeech txt2spch;
    private boolean hablar = false;
    private boolean motorHablaDisponible = false;

    public void iniciarT2S() {
        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        Log.i(TAG, "Initializing TextToSpeech...");
        txt2spch = new TextToSpeech(servicio,
            this  // TextToSpeech.OnInitListener
            );
    }
    
    public void apagarT2S() {
        Log.i(TAG, "Shutting Down TextToSpeech...");
        motorHablaDisponible = false;
        txt2spch.shutdown();
        Log.i(TAG, "TextToSpeech Shut Down.");
    }
    
    public void decir(String txt) {
        if (hablar && motorHablaDisponible) {
            txt2spch.speak(txt,
                    TextToSpeech.QUEUE_ADD,  // Drop all pending entries in the playback queue.
                    null);
        }
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int estado) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (estado == TextToSpeech.SUCCESS) {
        	Locale locSpanish = new Locale("spa", "MEX");
            int result = txt2spch.setLanguage(locSpanish);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Language data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                Log.i(TAG, "TextToSpeech Initialized.");
                motorHablaDisponible = true;
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    public void setHablar(boolean hablar) {
        this.hablar = hablar;
    }

    public boolean hablaHabilitada() {
        return hablar;
    }

    public boolean estoyHablando() {
        return txt2spch.isSpeaking();
    }

    public void ding() {}
    
    /********** Time **********/
    
    public static long tiempoActualEnMilisegs() {
        Time tiempo = new Time();
        tiempo.setToNow();
        return tiempo.toMillis(false);
    }
}

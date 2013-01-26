package com.osuarezl.cuentapasos;

import android.content.Context;
import android.os.Vibrator;

public class VibradorPasos implements EscuchaPasos {
    
    private Context mContext;
    private Vibrator mVibrator;
    
    public VibradorPasos(Context context) {
        mContext = context;
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    public void porPaso() {
        buzz();
    }
    
    public void valorEnviado() {
        
    }
    
    private void buzz() {
        mVibrator.vibrate(50);
    }
}
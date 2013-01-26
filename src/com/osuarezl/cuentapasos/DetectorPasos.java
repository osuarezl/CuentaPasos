package com.osuarezl.cuentapasos;

import java.util.ArrayList;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class DetectorPasos implements SensorEventListener {
	private final static String TAG = "StepDetector";
    private float   limite = 10;
    private float   ultimosValores[] = new float[3*2];
    private float   escala[] = new float[2];
    private float   offsetY;
    private float   ultimasDirecciones[] = new float[3*2];
    private float   ultimosExtremos[][] = { new float[3*2], new float[3*2] };
    private float   ultimoDif[] = new float[3*2];
    private int     ultimoAcierto = -1;
    private ArrayList<EscuchaPasos> escuchadoresPasos = new ArrayList<EscuchaPasos>();
    
    public DetectorPasos() {
        int h = 480; 
        offsetY = h * 0.5f;
        escala[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        escala[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }
    
    public void setSensibilidad(float sens) {
        limite = sens; 
    }
    
    public void agregarEscuchaPasos(EscuchaPasos ep) {
        escuchadoresPasos.add(ep);
    }
    
	@SuppressWarnings("deprecation")
	public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor; 
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            } else {
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    float vSum = 0;
                    for (int i=0 ; i<3 ; i++) {
                        final float v = offsetY + event.values[i] * escala[j];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum / 3; 
                    float direction = (v > ultimosValores[k] ? 1 : (v < ultimosValores[k] ? -1 : 0));
                    if (direction == - ultimasDirecciones[k]) {
                        // cambio en direccion
                        int extType = (direction > 0 ? 0 : 1); // min o max
                        ultimosExtremos[extType][k] = ultimosValores[k];
                        float diff = Math.abs(ultimosExtremos[extType][k] - ultimosExtremos[1 - extType][k]);

                        if (diff > limite) { 
                            boolean isAlmostAsLargeAsPrevious = diff > (ultimoDif[k]*2/3);
                            boolean isPreviousLargeEnough = ultimoDif[k] > (diff/3);
                            boolean isNotContra = (ultimoAcierto != 1 - extType);
                            
                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                Log.i(TAG, "step");
                                for (EscuchaPasos stepListener : escuchadoresPasos) {
                                    stepListener.porPaso();
                                }
                                ultimoAcierto = extType;
                            } else {
                                ultimoAcierto = -1;
                            }
                        }
                        ultimoDif[k] = diff;
                    }
                    ultimasDirecciones[k] = direction;
                    ultimosValores[k] = v;
                }
            }
        }
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}

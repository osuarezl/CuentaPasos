package com.osuarezl.cuentapasos;

import com.osuarezl.cuentapasos.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ServicioPasos extends Service  {
	private static final String TAG = "osuarezl.cuentapasos.ServicioPasos";
    private SharedPreferences configuracion;
    private CuentaPasos_Configuracion configuracion_cuentapasos;
    private SharedPreferences estado;
    private SharedPreferences.Editor editorEstado;
    private Utilidades utilidades;
    private SensorManager adminSensor;
    private Sensor sensor;
    private DetectorPasos detectorPasos;
    // private StepBuzzer mStepBuzzer; // used for debugging
    private DesplieguePasos desplieguePasos;
    private ControladorRitmo notificadorRitmo;
    private ControladorDistancia notificadorDistancia;
    private ControladorVelocidad notificadorVelocidad;
    private ControladorCalorias notificadorCalorias;
    private TemporizadorHabla temporizadorHabla;
    private PowerManager.WakeLock wakeLock;
    private NotificationManager notifManager;
    private int pasos;
    private int ritmo;
    private float distancia;
    private float velocidad;
    private float calorias;
    
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class BinderPasos extends Binder {
        ServicioPasos getServicio() {
            return ServicioPasos.this;
        }
    }
    
    @Override
    public void onCreate() {
        Log.i(TAG, "[SERVICE] onCreate");
        super.onCreate();
        
        notifManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        
        // Load settings
        configuracion = PreferenceManager.getDefaultSharedPreferences(this);
        configuracion_cuentapasos = new CuentaPasos_Configuracion(configuracion);
        estado = getSharedPreferences("state", 0);

        utilidades = Utilidades.getInstance();
        utilidades.serServicio(this);
        utilidades.iniciarT2S();

        obtenerWakeLock();
        
        // Start detecting
        detectorPasos = new DetectorPasos();
        adminSensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();

        // Register our receiver for the ACTION_SCREEN_OFF action. This will make our receiver
        // code be called whenever the phone enters standby mode.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receptor, filter);

        desplieguePasos = new DesplieguePasos(configuracion_cuentapasos, utilidades);
        desplieguePasos.setPasos(pasos = estado.getInt("steps", 0));
        desplieguePasos.agregarEscuha(escuhaPasos);
        detectorPasos.agregarEscuchaPasos(desplieguePasos);

        notificadorRitmo     = new ControladorRitmo(configuracion_cuentapasos, utilidades);
        notificadorRitmo.setRitmo(ritmo = estado.getInt("pace", 0));
        notificadorRitmo.agregarEscuha(escuchaRitmo);
        detectorPasos.agregarEscuchaPasos(notificadorRitmo);

        notificadorDistancia = new ControladorDistancia(escuchaDistancia, configuracion_cuentapasos, utilidades);
        notificadorDistancia.setDistancia(distancia = estado.getFloat("distance", 0));
        detectorPasos.agregarEscuchaPasos(notificadorDistancia);
        
        notificadorVelocidad    = new ControladorVelocidad(escuchaVelocidad,    configuracion_cuentapasos, utilidades);
        notificadorVelocidad.setVelocidad(velocidad = estado.getFloat("speed", 0));
        notificadorRitmo.agregarEscuha(notificadorVelocidad);
        
        notificadorCalorias = new ControladorCalorias(mCaloriesListener, configuracion_cuentapasos, utilidades);
        notificadorCalorias.setCalorias(calorias = estado.getFloat("calories", 0));
        detectorPasos.agregarEscuchaPasos(notificadorCalorias);
        
        temporizadorHabla = new TemporizadorHabla(configuracion_cuentapasos, utilidades);
        temporizadorHabla.agregarEscuha(desplieguePasos);
        temporizadorHabla.agregarEscuha(notificadorRitmo);
        temporizadorHabla.agregarEscuha(notificadorDistancia);
        temporizadorHabla.agregarEscuha(notificadorVelocidad);
        temporizadorHabla.agregarEscuha(notificadorCalorias);
        detectorPasos.agregarEscuchaPasos(temporizadorHabla);
        
        // Used when debugging:
        // mStepBuzzer = new StepBuzzer(this);
        // mStepDetector.addStepListener(mStepBuzzer);

        // Start voice
        cargarConfiguracion();

        // Tell the user we started.
        Toast.makeText(this, getText(R.string.started), Toast.LENGTH_SHORT).show();
    }
    
	@SuppressWarnings("deprecation")
	@Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "[SERVICE] onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "[SERVICE] onDestroy");
        utilidades.apagarT2S();

        // Unregister our receiver.
        unregisterReceiver(receptor);
        desregistrarSensor();
        
        editorEstado = estado.edit();
        editorEstado.putInt("steps", pasos);
        editorEstado.putInt("pace", ritmo);
        editorEstado.putFloat("distance", distancia);
        editorEstado.putFloat("speed", velocidad);
        editorEstado.putFloat("calories", calorias);
        editorEstado.commit();
        
        notifManager.cancel(R.string.app_name);

        wakeLock.release();
        
        super.onDestroy();
        
        // Stop detecting
        adminSensor.unregisterListener(detectorPasos);

        // Tell the user we stopped.
        Toast.makeText(this, getText(R.string.stopped), Toast.LENGTH_SHORT).show();
    }

    private void registerDetector() {
        sensor = adminSensor.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER /*| 
            Sensor.TYPE_MAGNETIC_FIELD | 
            Sensor.TYPE_ORIENTATION*/);
        adminSensor.registerListener(detectorPasos,
            sensor,
            SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void desregistrarSensor() {
        adminSensor.unregisterListener(detectorPasos);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "[SERVICE] onBind");
        return mBinder;
    }

    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new BinderPasos();

    public interface ICallback {
        public void pasosCambiados(int value);
        public void cambioRitmo(int value);
        public void cambioDistancia(float value);
        public void cambioVelocidad(float value);
        public void cambioCalorias(float value);
    }
    
    private ICallback callBack;

    public void registerCallback(ICallback cb) {
        callBack = cb;
        //mStepDisplayer.passValue();
        //mPaceListener.passValue();
    }
    
    private int ritmoDeseado;
    private float velocidadDeseada;
    
    /**
     * Called by activity to pass the desired pace value, 
     * whenever it is modified by the user.
     * @param desiredPace
     */
    public void setRitmoDeseado(int ritmoDeseado) {
        this.ritmoDeseado = ritmoDeseado;
        if (notificadorRitmo != null) {
            notificadorRitmo.setRitmoDeseado(ritmoDeseado);
        }
    }
    /**
     * Called by activity to pass the desired speed value, 
     * whenever it is modified by the user.
     * @param desiredSpeed
     */
    public void setVelocidadDeseada(float velocidadDeseada) {
        this.velocidadDeseada = velocidadDeseada;
        if (notificadorVelocidad != null) {
            notificadorVelocidad.setVelocidadDeseada(velocidadDeseada);
        }
    }
    
    public void cargarConfiguracion() {
        configuracion = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (detectorPasos != null) { 
            detectorPasos.setSensibilidad(
                    Float.valueOf(configuracion.getString("sensitivity", "10"))
            );
        }
        
        if (desplieguePasos    != null) desplieguePasos.caargarConfiguracion();
        if (notificadorRitmo     != null) notificadorRitmo.cargarConfiguracion();
        if (notificadorDistancia != null) notificadorDistancia.cargarConfiguracion();
        if (notificadorVelocidad    != null) notificadorVelocidad.cargarConfiguracion();
        if (notificadorCalorias != null) notificadorCalorias.cargarConfiguracion();
        if (temporizadorHabla    != null) temporizadorHabla.cargarConfiguracion();
    }
    
    public void restablecerValores() {
        desplieguePasos.setPasos(0);
        notificadorRitmo.setRitmo(0);
        notificadorDistancia.setDistancia(0);
        notificadorVelocidad.setVelocidad(0);
        notificadorCalorias.setCalorias(0);
    }
    
    /**
     * Forwards pace values from PaceNotifier to the activity. 
     */
    private DesplieguePasos.Detector escuhaPasos = new DesplieguePasos.Detector() {
        public void pasosCambiados(int value) {
            pasos = value;
            valorEnviado();
        }
        public void valorEnviado() {
            if (callBack != null) {
                callBack.pasosCambiados(pasos);
            }
        }
    };
    /**
     * Forwards pace values from PaceNotifier to the activity. 
     */
    private ControladorRitmo.Detector escuchaRitmo = new ControladorRitmo.Detector() {
        public void cambioRitmo(int value) {
            ritmo = value;
            valorEnviado();
        }
        public void valorEnviado() {
            if (callBack != null) {
                callBack.cambioRitmo(ritmo);
            }
        }
    };
    /**
     * Forwards distance values from DistanceNotifier to the activity. 
     */
    private ControladorDistancia.Detector escuchaDistancia = new ControladorDistancia.Detector() {
        public void cambioValor(float value) {
            distancia = value;
            valorEnviado();
        }
        public void valorEnviado() {
            if (callBack != null) {
                callBack.cambioDistancia(distancia);
            }
        }
    };
    /**
     * Forwards speed values from SpeedNotifier to the activity. 
     */
    private ControladorVelocidad.Detector escuchaVelocidad = new ControladorVelocidad.Detector() {
        public void cambioValor(float value) {
            velocidad = value;
            valorEnviado();
        }
        public void valorEnviado() {
            if (callBack != null) {
                callBack.cambioVelocidad(velocidad);
            }
        }
    };
    /**
     * Forwards calories values from CaloriesNotifier to the activity. 
     */
    private ControladorCalorias.Detector mCaloriesListener = new ControladorCalorias.Detector() {
        public void cambioValor(float value) {
            calorias = value;
            valorEnviado();
        }
        public void valorEnviado() {
            if (callBack != null) {
                callBack.cambioCalorias(calorias);
            }
        }
    };
    
    /**
     * Show a notification while this service is running.
     */
	@SuppressWarnings("deprecation")
	private void showNotification() {
        CharSequence text = getText(R.string.app_name);
        Notification notification = new Notification(R.drawable.ic_launcher, null, System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        Intent intentoCuentaPasos = new Intent();
        intentoCuentaPasos.setComponent(new ComponentName(this, CuentaPasos.class));
        intentoCuentaPasos.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intentoCuentaPasos, 0);
        notification.setLatestEventInfo(this, text, getText(R.string.notification_subtitle), contentIntent);

        notifManager.notify(R.string.app_name, notification);
    }


    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    private BroadcastReceiver receptor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Unregisters the listener and registers it again.
                ServicioPasos.this.desregistrarSensor();
                ServicioPasos.this.registerDetector();
                if (configuracion_cuentapasos.despertarForzado()) {
                    wakeLock.release();
                    obtenerWakeLock();
                }
            }
        }
    };

	@SuppressWarnings("deprecation")
	private void obtenerWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int banderas;
        if (configuracion_cuentapasos.despertarForzado()) {
            banderas = PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
        }
        else if (configuracion_cuentapasos.mantenerPantallaEncendida()) {
            banderas = PowerManager.SCREEN_DIM_WAKE_LOCK;
        }
        else {
            banderas = PowerManager.PARTIAL_WAKE_LOCK;
        }
        wakeLock = pm.newWakeLock(banderas, TAG);
        wakeLock.acquire();
    }
}

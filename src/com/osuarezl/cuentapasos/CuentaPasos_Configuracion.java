package com.osuarezl.cuentapasos;

import android.content.SharedPreferences;

public class CuentaPasos_Configuracion {
SharedPreferences configuracion;
    
    public static int NADA = 1;
    public static int RITMO = 2;
    public static int VELOCIDAD = 3;
    
    public CuentaPasos_Configuracion(SharedPreferences configuracion) {
        this.configuracion = configuracion;
    }
    
    public boolean esSI() {
        return configuracion.getString("units", "imperial").equals("metric");
    }
    
    public float getTamPaso() {
        try {
            return Float.valueOf(configuracion.getString("step_length", "20").trim());
        } catch (NumberFormatException e) {
            // TODO: setear valor y avisar al usuario
            return 0f;
        }
    }
    
    public float getPesoCuerpo() {
        try {
            return Float.valueOf(configuracion.getString("body_weight", "50").trim());
        } catch (NumberFormatException e) {
        	// TODO: setear valor y avisar al usuario
            return 0f;
        }
    }

    public boolean estaCorriendo() {
        return configuracion.getString("exercise_type", "running").equals("running");
    }

    public int getOpcionMantenimiento() {
        String p = configuracion.getString("maintain", "none");
        return 
            p.equals("none") ? NADA : (
            p.equals("pace") ? RITMO : (
            p.equals("speed") ? VELOCIDAD : ( 
            0)));
    }
    
    //-------------------------------------------------------------------
    // Desired pace & speed: 
    // these can not be set in the preference activity, only on the main
    // screen if "maintain" is set to "pace" or "speed" 
    public int getRitmoDeseado() {
        return configuracion.getInt("desired_pace", 180); // steps/minute
    }
    
    public float getVelocidadDeseada() {
        return configuracion.getFloat("desired_speed", 4f); // km/h or mph
    }
    
    public void guardarRitmoOVelocidad(int maintain, float deseado) {
        SharedPreferences.Editor editor = configuracion.edit();
        if (maintain == RITMO) {
            editor.putInt("desired_pace", (int)deseado);
        } else if (maintain == VELOCIDAD) {
            editor.putFloat("desired_speed", deseado);
        }
        editor.commit();
    }
    
    //-------------------------------------------------------------------
    // Speaking:
    public boolean deboHablar() {
        return configuracion.getBoolean("speak", false);
    }
    
    public float getIntervaloHabla() {
        try {
            return Float.valueOf(configuracion.getString("speaking_interval", "1"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
    
    public boolean deboDecirPasos() {
        return configuracion.getBoolean("speak", false) && configuracion.getBoolean("tell_steps", false);
    }
    
    public boolean deboDecirRitmo() {
        return configuracion.getBoolean("speak", false) && configuracion.getBoolean("tell_pace", false);
    }
    
    public boolean debeDecirDistancia() {
        return configuracion.getBoolean("speak", false) && configuracion.getBoolean("tell_distance", false);
    }
    
    public boolean deboDecirVelocidad() {
        return configuracion.getBoolean("speak", false) && configuracion.getBoolean("tell_speed", false);
    }
    
    public boolean debeDecirCalorias() {
        return configuracion.getBoolean("speak", false) && configuracion.getBoolean("tell_calories", false);
    }
    
    public boolean deoboDecirAjusteVelocidad() {
        return configuracion.getBoolean("speak", false) && configuracion.getBoolean("tell_fasterslower", false);
    }
    
    public boolean despertarForzado() {
        return configuracion.getString("operation_level", "run_in_background").equals("wake_up");
    }
    
    public boolean mantenerPantallaEncendida() {
        return configuracion.getString("operation_level", "run_in_background").equals("keep_screen_on");
    }
    
    //
    // Internal  
    public void guardarServicioCorriendoConTiempo(boolean running) {
        SharedPreferences.Editor editor = configuracion.edit();
        editor.putBoolean("service_running", running);
        editor.putLong("last_seen", Utilidades.tiempoActualEnMilisegs());
        editor.commit();
    }
    
    public void guardarServicioConTiempoNull(boolean running) {
        SharedPreferences.Editor editor = configuracion.edit();
        editor.putBoolean("service_running", running);
        editor.putLong("last_seen", 0);
        editor.commit();
    }

    public void limpiarServicioCorriendo() {
        SharedPreferences.Editor editor = configuracion.edit();
        editor.putBoolean("service_running", false);
        editor.putLong("last_seen", 0);
        editor.commit();
    }

    public boolean estaCorriendoServicio() {
        return configuracion.getBoolean("service_running", false);
    }
    
    public boolean esNuevoComienzo() {
        // activity last paused more than 10 minutes ago
        return configuracion.getLong("last_seen", 0) < Utilidades.tiempoActualEnMilisegs() - 1000*60*10;
    }
}

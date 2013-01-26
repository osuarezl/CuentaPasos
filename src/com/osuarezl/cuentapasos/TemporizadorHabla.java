package com.osuarezl.cuentapasos;

import java.util.ArrayList;

public class TemporizadorHabla implements EscuchaPasos {
	CuentaPasos_Configuracion configuracion;
    Utilidades utilidades;
    boolean deboHablar;
    float intervalo;
    long ultimaVezHable;
    
    public TemporizadorHabla(CuentaPasos_Configuracion configuracion, Utilidades utilidades) {
        this.ultimaVezHable = System.currentTimeMillis();
        this.configuracion = configuracion;
        this.utilidades = utilidades;
        cargarConfiguracion();
    }
    
    public void cargarConfiguracion() {
        deboHablar = configuracion.deboHablar();
        intervalo = configuracion.getIntervaloHabla();
    }
    
    public void porPaso() {
        long ahora = System.currentTimeMillis();
        long delta = ahora - ultimaVezHable;
        
        if (delta / 60000.0 >= intervalo) {
            ultimaVezHable = ahora;
            notificarEscuchas();
        }
    }
    
    public void valorEnviado() { }

    
    //-----------------------------------------------------
    // Listener
    
    public interface Detector {
        public void hablar();
    }
    
    private ArrayList<Detector> escuchas = new ArrayList<Detector>();

    public void agregarEscuha(Detector d) {
        escuchas.add(d);
    }
    
    public void notificarEscuchas() {
        utilidades.ding();
        for (Detector listener : escuchas) {
            listener.hablar();
        }
    }

    //-----------------------------------------------------
    // Speaking
    
    public boolean estaHablando() {
        return utilidades.estoyHablando();
    }
}

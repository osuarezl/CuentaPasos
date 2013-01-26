package com.osuarezl.cuentapasos;

import java.util.ArrayList;

public class DesplieguePasos implements EscuchaPasos, TemporizadorHabla.Detector {
	private int contador = 0;
    CuentaPasos_Configuracion configuracion;
    Utilidades utilidades;

    public DesplieguePasos(CuentaPasos_Configuracion configuracion, Utilidades utilidades) {
        this.utilidades = utilidades;
        this.configuracion = configuracion;
        notificarEscuha();
    }
    
    public void setUtilidades(Utilidades utilidades) {
    	this.utilidades = utilidades;
    }

    public void setPasos(int pasos) {
    	this.contador = pasos;
        notificarEscuha();
    }
    
    public void porPaso() {
        contador ++;
        notificarEscuha();
    }
    public void caargarConfiguracion() {
        notificarEscuha();
    }
    public void valorEnviado() {
    }
    
    

    //-----------------------------------------------------
    // Listener
    
    public interface Detector {
        public void pasosCambiados(int valor);
        public void valorEnviado();
    }
    
    private ArrayList<Detector> escuchas = new ArrayList<Detector>();

    public void agregarEscuha(Detector d) {
        escuchas.add(d);
    }
    
    public void notificarEscuha() {
        for (Detector escucha : escuchas) {
            escucha.pasosCambiados((int)contador);
        }
    }
    
    //-----------------------------------------------------
    // Speaking
    
    public void hablar() {
        if (configuracion.deboDecirPasos()) { 
            if (contador > 0) {
                utilidades.decir("" + contador + " pasos");
            }
        }
    }
}

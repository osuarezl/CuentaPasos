package com.osuarezl.cuentapasos;

public class ControladorDistancia implements EscuchaPasos, TemporizadorHabla.Detector {

    public interface Detector {
        public void cambioValor(float valor);
        public void valorEnviado();
    }
    
    private Detector escucha;
    float distancia = 0;
    CuentaPasos_Configuracion configuracion;
    Utilidades utilidades;
    boolean esSI;
    float tamPaso;

    public ControladorDistancia(Detector escucha, CuentaPasos_Configuracion configuracion, Utilidades utilidades) {
        this.escucha = escucha;
        this.utilidades = utilidades;
        this.configuracion = configuracion;
        cargarConfiguracion();
    }
    
    public void setDistancia(float distancia) {
        this.distancia = distancia;
        notificarEscucha();
    }
    
    public void cargarConfiguracion() {
        esSI = configuracion.esSI();
        tamPaso = configuracion.getTamPaso();
        notificarEscucha();
    }
    
    public void porPaso() {
        
        if (esSI) {
            distancia += (float)(tamPaso / 100000.0); 
        } else {
            distancia += (float)(tamPaso / 63360.0); 
        }
        notificarEscucha();
    }
    
    private void notificarEscucha() {
        escucha.cambioValor(distancia);
    }
    
    public void valorEnviado() { }

    public void hablar() {
        if (configuracion.debeDecirDistancia()) {
            if (distancia >= .001f) {
                utilidades.decir(("" + (distancia + 0.000001f)).substring(0, 4) + (esSI ? " kilómetros" : " millas"));
                // TODO: format numbers (no "." at the end)
            }
        }
    }
    
}



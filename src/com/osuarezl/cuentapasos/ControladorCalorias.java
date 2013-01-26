package com.osuarezl.cuentapasos;

public class ControladorCalorias implements EscuchaPasos, TemporizadorHabla.Detector {
    
	public interface Detector {
        public void cambioValor(float valor);
        public void valorEnviado();
    }
    
    private Detector escuha;
    private static double Fac_Correr_SI = 1.02784823;
    private static double Fac_Cam_SI = 0.708;
    private static double Fac_Correr_US = 0.75031498;
    private static double Fac_Cam_US = 0.517;
    private double calorias = 0;
    CuentaPasos_Configuracion configuracion;
    Utilidades utilidades;
    boolean esSI;
    boolean estaCorriendo;
    float tamPaso;
    float peso;

    public ControladorCalorias(Detector escucha, CuentaPasos_Configuracion configuracion, Utilidades utilidades) {
        this.escuha = escucha;
        this.utilidades = utilidades;
        this.configuracion = configuracion;
        cargarConfiguracion();
    }
    
    public void setCalorias(float calorias) {
        this.calorias = calorias;
        notificarEscuha();
    }
    
    public void cargarConfiguracion() {
        esSI = configuracion.esSI();
        estaCorriendo = configuracion.estaCorriendo();
        tamPaso = configuracion.getTamPaso();
        peso = configuracion.getPesoCuerpo();
        notificarEscuha();
    }
    public void reiniciarValores() {
        calorias = 0;
    }
    
    public void es_SI(boolean esSI) {
        this.esSI = esSI;
    }
    
    public void setTamPaso(float tamPaso) {
        this.tamPaso = tamPaso;
    }
    
    public void porPaso() {
        if (esSI) {
            calorias += (peso * (estaCorriendo ? Fac_Correr_SI : Fac_Cam_SI)) * tamPaso / 100000.0; 
        } else {
            calorias += (peso * (estaCorriendo ? Fac_Correr_US : Fac_Cam_US)) * tamPaso / 63360.0;            
        }
        notificarEscuha();
    }
    
    private void notificarEscuha() {
        escuha.cambioValor((float)calorias);
    }
    
    public void valorEnviado() {}
    
    public void hablar() { 
        if (configuracion.debeDecirCalorias()) {
            if (calorias > 0) {
                utilidades.decir("" + (int)calorias + " calorías quemadas");
            }
        }   
    }

}


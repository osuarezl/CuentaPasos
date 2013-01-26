package com.osuarezl.cuentapasos;

import java.util.ArrayList;

public class ControladorRitmo implements EscuchaPasos, TemporizadorHabla.Detector {

    public interface Detector {
        public void cambioRitmo(int valor);
        public void valorEnviado();
    }
    
    private ArrayList<Detector> escuchas = new ArrayList<Detector>();
    int contador = 0;
    private long tiempoUltimoPaso = 0;
    private long[] deltas = {-1, -1, -1, -1};
    private int idxUltimoPaso = 0;
    private long ritmo = 0;
    CuentaPasos_Configuracion configuracion;
    Utilidades utilidades;
    int ritmoDeseado;
    boolean deboHablar;
    private long haceCuantoHable = 0;

    public ControladorRitmo(CuentaPasos_Configuracion configuracion, Utilidades utilidades) {
        this.utilidades = utilidades;
        this.configuracion = configuracion;
        this.ritmoDeseado = this.configuracion.getRitmoDeseado();
        cargarConfiguracion();
    }
    
    public void setRitmo(int ritmo) {
        this.ritmo = ritmo;
        int promedio = (int)(60*1000.0 / ritmo);
        for (int i = 0; i < deltas.length; i++) {
            deltas[i] = promedio;
        }
        notificarEscucha();
    }
    
    public void cargarConfiguracion() {
        deboHablar = configuracion.deoboDecirAjusteVelocidad() && configuracion.getOpcionMantenimiento() == CuentaPasos_Configuracion.RITMO;
        notificarEscucha();
    }
    
    public void agregarEscuha(Detector d) {
        escuchas.add(d);
    }

    public void setRitmoDeseado(int ritmoDeseado) {
        this.ritmoDeseado = ritmoDeseado;
    }

    public void porPaso() {
        long tiempoEstePaso = System.currentTimeMillis();
        contador ++;
        // Calcular ritmo
        if (tiempoUltimoPaso > 0) {
            long delta = tiempoEstePaso - tiempoUltimoPaso;
            deltas[idxUltimoPaso] = delta;
            idxUltimoPaso = (idxUltimoPaso + 1) % deltas.length;
            long sum = 0;
            boolean importa = true;
            for (int i = 0; i < deltas.length; i++) {
                if (deltas[i] < 0) {
                    importa = false;
                    break;
                }
                sum += deltas[i];
            }
            if (importa && sum > 0) {
                long promedio = sum / deltas.length;
                ritmo = 60*1000 / promedio;
                if (deboHablar && !utilidades.hablaHabilitada()) {
                    if (tiempoEstePaso - haceCuantoHable > 3000 && !utilidades.estoyHablando()) {
                        float poco = 0.10f;
                        float normal = 0.30f;
                        float mucho = 0.50f;
                        boolean hablado = true;
                        if (ritmo < ritmoDeseado * (1 - mucho)) {
                            utilidades.decir("mucho más rápido!");
                        } else if (ritmo > ritmoDeseado * (1 + mucho)) {
                            utilidades.decir("mucho más lento!");
                        } else if (ritmo < ritmoDeseado * (1 - normal)) {
                            utilidades.decir("más rápido!");
                        } else if (ritmo > ritmoDeseado * (1 + normal)) {
                            utilidades.decir("más lento!");
                        } else if (ritmo < ritmoDeseado * (1 - poco)) {
                            utilidades.decir("un poco más rápido!");
                        } else if (ritmo > ritmoDeseado * (1 + poco)) {
                            utilidades.decir("un poco más lento!");
                        } else {
                            hablado = false;
                        }
                        if (hablado) {
                            haceCuantoHable = tiempoEstePaso;
                        }
                    }
                }
            } else {
                ritmo = -1;
            }
        }
        tiempoUltimoPaso = tiempoEstePaso;
        notificarEscucha();
    }
    
    private void notificarEscucha() {
        for (Detector escucha : escuchas) {
            escucha.cambioRitmo((int)ritmo);
        }
    }
    
    public void valorEnviado() { }
    
    public void hablar() {
        if (configuracion.deboDecirRitmo()) {
            if (ritmo > 0) {
                utilidades.decir(ritmo + " pasos por minuto");
            }
        }
    }
    

}



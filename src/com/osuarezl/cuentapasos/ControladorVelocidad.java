package com.osuarezl.cuentapasos;

//Clase controladora de la Velocidad
public class ControladorVelocidad implements ControladorRitmo.Detector, TemporizadorHabla.Detector {

  public interface Detector {
      public void cambioValor(float valor);
      public void valorEnviado();
  }
  
  private Detector escucha;
  int contador = 0;
  float velocidad = 0;
  boolean esSI;
  float tamPaso;
  CuentaPasos_Configuracion configuracion;
  Utilidades utilidades;
  float velocidadDeseada;
  boolean deboDecirAjusteVelocidad;
  boolean deboDecirVelocidad;
  private long haceCuantoHable = 0;
  
  public ControladorVelocidad(Detector escucha, CuentaPasos_Configuracion configuracion, Utilidades utilidades) {
      this.escucha = escucha;
      this.utilidades = utilidades;
      this.configuracion = configuracion;
      this.velocidadDeseada = this.configuracion.getVelocidadDeseada();
      cargarConfiguracion();
  }
  
  public void setVelocidad(float velocidad) {
      this.velocidad = velocidad;
      notificarEscucha();
  }
  
  public void cargarConfiguracion() {
      esSI = configuracion.esSI();
      tamPaso = configuracion.getTamPaso();
      deboDecirVelocidad = configuracion.deboDecirVelocidad();
      deboDecirAjusteVelocidad = configuracion.deoboDecirAjusteVelocidad() && configuracion.getOpcionMantenimiento() == CuentaPasos_Configuracion.VELOCIDAD;
      notificarEscucha();
  }
  
  public void setVelocidadDeseada(float velocidadDeseada) {
      this.velocidadDeseada = velocidadDeseada;
  }
  
  private void notificarEscucha() {
      escucha.cambioValor(velocidad);
  }
  
  public void cambioRitmo(int valor) {
      if (esSI) {
          velocidad = valor * tamPaso / 100000f * 60f; 
      } else {
          velocidad = valor * tamPaso / 63360f * 60f; 
      }
      indicarCambioVelocidad();
      notificarEscucha();
  }
  

  private void indicarCambioVelocidad() {
      if (deboDecirAjusteVelocidad && utilidades.hablaHabilitada()) {
          long now = System.currentTimeMillis();
          if (now - haceCuantoHable > 3000 && !utilidades.estoyHablando()) {
              float poco = 0.10f;
              float normal = 0.30f;
              float mucho = 0.50f;
              boolean hablado = true;
              if (velocidad < velocidadDeseada * (1 - mucho)) {
                  utilidades.decir("mucho más rápido!");
              } else if (velocidad > velocidadDeseada * (1 + mucho)) {
                  utilidades.decir("mucho más lento!");
              } else if (velocidad < velocidadDeseada * (1 - normal)) {
                  utilidades.decir("más rápido!");
              } else if (velocidad > velocidadDeseada * (1 + normal)) {
                  utilidades.decir("más lento!");
              } else if (velocidad < velocidadDeseada * (1 - poco)) {
                  utilidades.decir("un poco más rápido!");
              } else if (velocidad > velocidadDeseada * (1 + poco)) {
                  utilidades.decir("un poco más lento!");
              } else {
                  hablado = false;
              }
              if (hablado) {
                  haceCuantoHable = now;
              }
          }
      }
  }
  
  public void valorEnviado() { }

  public void hablar() {
      if (configuracion.deboDecirVelocidad()) {
          if (velocidad >= .01f) {
              utilidades.decir(("" + (velocidad + 0.000001f)).substring(0, 4) + (esSI ? " kilómetros por hora" : " millas por hora"));
          }
      }
      
  }

}



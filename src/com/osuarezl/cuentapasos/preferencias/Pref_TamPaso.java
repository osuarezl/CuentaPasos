package com.osuarezl.cuentapasos.preferencias;


import com.osuarezl.cuentapasos.R;

import android.content.Context;
import android.util.AttributeSet;

public class Pref_TamPaso extends Pref_SistemaMedidas {

	public Pref_TamPaso(Context contexto) {
		super(contexto);
	}
	
	public Pref_TamPaso(Context contexto, AttributeSet atribs) {
		super(contexto, atribs);
	}
	
	public Pref_TamPaso(Context contexto, AttributeSet atribs, int estilo) {
		super(contexto, atribs, estilo);
	}

	protected void inicializarDetallesPrefs() {
		recursoTitulo = R.string.step_length_setting_title;
		recursoUnidadesSI = R.string.centimeters;
		revursoUnidadesUS = R.string.inches;
	}
}


package com.osuarezl.cuentapasos.preferencias;

import com.osuarezl.cuentapasos.R;
import android.content.Context;
import android.util.AttributeSet;

public class Pref_PesoCorporal extends Pref_SistemaMedidas {

	public Pref_PesoCorporal(Context contexto) {
		super(contexto);
	}
	
	public Pref_PesoCorporal(Context contexto, AttributeSet atribs) {
		super(contexto, atribs);
	}
	
	public Pref_PesoCorporal(Context contexto, AttributeSet atribs, int estilo) {
		super(contexto, atribs, estilo);
	}
	
	protected void inicializarDetallesPrefs() {
		recursoTitulo = R.string.body_weight_setting_title;
		recursoUnidadesSI = R.string.kilograms;
		revursoUnidadesUS = R.string.pounds;
	}
}


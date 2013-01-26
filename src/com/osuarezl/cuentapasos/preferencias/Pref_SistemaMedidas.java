package com.osuarezl.cuentapasos.preferencias;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

abstract public class Pref_SistemaMedidas extends EditTextPreference {
	boolean esSI;
	
	protected int recursoTitulo;
	protected int recursoUnidadesSI;
	protected int revursoUnidadesUS;
	
	public Pref_SistemaMedidas(Context contexto) {
		super(contexto);
		inicializarDetallesPrefs();
	}
	
	public Pref_SistemaMedidas(Context context, AttributeSet atribs) {
		super(context, atribs);
		inicializarDetallesPrefs();
	}
	
	public Pref_SistemaMedidas(Context context, AttributeSet atribs, int estilo) {
		super(context, atribs, estilo);
		inicializarDetallesPrefs();
	}
	
	abstract protected void inicializarDetallesPrefs();
	
	protected void showDialog(Bundle state) {
		esSI = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("units", "imperial").equals("metric");
		setDialogTitle(
				getContext().getString(recursoTitulo) + 
				" (" + 
						getContext().getString(
								esSI
								? recursoUnidadesSI 
								: revursoUnidadesUS) + 
				")"
		);
		
		try {
			Float.valueOf(getText());
		}
		catch (Exception e) {
			setText("20");
		}
		
		super.showDialog(state);
	}
	
	protected void onAddEditTextToDialogView (View dialogView, EditText editText) {
		editText.setRawInputType(
				InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			try {
				Float.valueOf(((CharSequence)(getEditText().getText())).toString());
			}
			catch (NumberFormatException e) {
				this.showDialog(null);
				return;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}

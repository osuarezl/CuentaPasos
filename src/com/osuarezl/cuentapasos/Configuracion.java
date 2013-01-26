package com.osuarezl.cuentapasos;

import com.osuarezl.cuentapasos.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Configuracion extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle estadoSalvado) {
        super.onCreate(estadoSalvado);
        addPreferencesFromResource(R.xml.preferences);
    }
}

package com.projectgame.projectgame;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class mostrarAyuda {

    private final Context context;

    // Constructor que recibe el contexto
    public mostrarAyuda(Context context) {
        this.context = context;
    }

    public void mostrar() {
        // Inflar el diseño personalizado
        View dialogView = LayoutInflater.from(context).inflate(R.layout.help_dialog_layout, null);

        // Crear el AlertDialog con el diseño personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}

package com.imaginamos.taxisya.taxista.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.imaginamos.taxisya.taxista.R;

public class Dialogos {

    int id_message = 0;
    AlertDialog dialog;

    public Dialogos(Context context, int idtext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.setTitle(context.getString(R.string.dialog_titulo_taxisya));
        dialog.setMessage(context.getResources().getString(idtext));
        dialog.show();
    }


}

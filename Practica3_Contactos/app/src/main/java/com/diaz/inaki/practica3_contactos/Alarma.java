package com.diaz.inaki.practica3_contactos;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by inaki on 22/4/17.
 */

public class Alarma extends BroadcastReceiver {
    public static Modelo mod;

    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        System.out.println("Alarma disparada!!!");
        CharSequence text = "Alarma disparada!!!!";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        Calendar mcurrentTime = Calendar.getInstance();
        String day = String.format("%2d", mcurrentTime.get(Calendar.DATE));
        String month = String.format("%2d", mcurrentTime.get(Calendar.MONTH) + 1); //da los meses en base 0

        String hoy = month + "-" + day;
        //System.out.println(day + " de " + month);
        if (mod == null) { //si la aplicación esta cerrada, instanciamos un modelo para tener el array de contactos
            mod = new Modelo(context);
        }

        Boolean notificacionNecesaria = false;
        List<String> listaNombres = new ArrayList<>();
        for (Contacto c : mod.getListaContactos()
                ) {
            if (c.getFechaNacimiento() != "") {
                String fNaci = c.getFechaNacimiento();
                String[] fechas = fNaci.split("-");//año 0 - mes 1 - día 2
                if (fechas.length == 3) {
                    String cumple = String.format("%2d", Integer.parseInt(fechas[1])) + "-" + String.format("%2d", Integer.parseInt(fechas[2]));
                    if (hoy.equals(cumple)) {
                        notificacionNecesaria = true;
                        listaNombres.add(c.getName());
                        if (c.getTipoNotif() == 'y') {
                            enviarSMS(c);
                        }
                    }
                }

            }
        }
        if (notificacionNecesaria) {
            notificación(listaNombres);
        }
    }

    private void enviarSMS(Contacto c) {

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(c.getTelefono(), null, c.getMensaje(), null, null);
            Toast.makeText(context, "SMS enviado.",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
                    Toast.makeText(context, "SMS no enviado, por favor, inténtalo otra vez.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void notificación(List<String> nombres) {
       Notificacion notificacion= new Notificacion(context, nombres);



    }
}


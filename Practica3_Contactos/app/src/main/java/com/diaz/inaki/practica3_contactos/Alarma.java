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

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;//copiamos el contexto
        if (MainActivity.DEBUG) {
            System.out.println(R.string.alarma);
        }
        CharSequence text = context.getString(R.string.alarma);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();//una tostada avisando de la alarma
        //preparamos un calendario
        Calendar mcurrentTime = Calendar.getInstance();
        String day = String.format("%2d", mcurrentTime.get(Calendar.DATE));//cogemos el día
        String month = String.format("%2d", mcurrentTime.get(Calendar.MONTH) + 1); //da los meses en base 0

        //preparamos una cadena
        String hoy = month + "-" + day;
        //si la aplicación esta cerrada, instanciamos un modelo para tener el array de contactos
        if (mod == null) {
            mod = new Modelo(context);
        }

        Boolean notificacionNecesaria = false;//si hay cumpleaños hoy pasara a true
        List<String> listaNombres = new ArrayList<>(); //lista temporal de cumpleañeros
        for (Contacto c : mod.getListaContactos()
                ) {
            if (c.getFechaNacimiento() != "") {
                String fNaci = c.getFechaNacimiento();
                String[] fechas = fNaci.split("-");//año 0 - mes 1 - día 2
                if (fechas.length == 3) {//si el array es de tres es que ha sido bien inicializado
                    //cadena para compara fechas
                    String cumple = String.format("%2d", Integer.parseInt(fechas[1])) + "-" + String.format("%2d", Integer.parseInt(fechas[2]));
                    if (hoy.equals(cumple)) {
                        notificacionNecesaria = true;//se sacará una notificación
                        listaNombres.add(c.getName());
                        if (c.getTipoNotif() == 'y') {//si esta marcado enviar sms
                            enviarSMS(c);
                        }
                    }
                }

            }
        }
        if (notificacionNecesaria) {
            notificación(listaNombres);//notificación con todos los cumpleañeros
        }
    }

    //función para enviar sms
    private void enviarSMS(Contacto c) {

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(c.getTelefono(), null, c.getMensaje(), null, null);
            Toast.makeText(context, R.string.smsok, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, R.string.smsnotok, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //función para enviar una notificación extendida
    private void notificación(List<String> nombres) {
        Notificacion notificacion = new Notificacion(context, nombres);


    }
}


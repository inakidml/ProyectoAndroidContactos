package com.diaz.inaki.practica3_contactos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by inaki on 22/4/17.
 */

public class Alarma extends BroadcastReceiver {
    public static Modelo mod;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Alarma disparada!!!");
        CharSequence text = "Alarma disparada!!!!";
        int duration = Toast.LENGTH_SHORT;
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
        String textoNotificacion = "Hoy es el cumpleaños de: \n";

        for (Contacto c : mod.getListaContactos()
                ) {
            if (c.getFechaNacimiento() != "") {
                String fNaci = c.getFechaNacimiento();
                String[] fechas = fNaci.split("-");//año 0 - mes 1 - día 2
                if (fechas.length == 3) {
                    String cumple = String.format("%2d", Integer.parseInt(fechas[1])) + "-" + String.format("%2d", Integer.parseInt(fechas[2]));
                    if (hoy.equals(cumple)) {
                        notificacionNecesaria = true;
                        //TODO notificación y sms
                        System.out.println("Hoy es el cumple de: ");
                        System.out.println(c);
                        textoNotificacion += c.getName() + "\n";
                        if (c.getTipoNotif()=='y'){
                            enviarSMS(c);
                        }
                    }
                }

            }
        }
        if (notificacionNecesaria){
            notificación(textoNotificacion);
        }

    }
    private void enviarSMS(Contacto c){}
    private void notificación(String nota){}
}


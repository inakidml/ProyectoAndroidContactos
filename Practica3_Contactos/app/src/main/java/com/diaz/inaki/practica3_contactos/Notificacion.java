package com.diaz.inaki.practica3_contactos;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.widget.Button;

import com.diaz.inaki.practica3_contactos.MainActivity;

import java.util.List;

/**
 * Created by inaki on 28/4/17.
 */

public class Notificacion {

    private Context context;

    //constructor
    public Notificacion(Context context, List<String> nombres) {
        this.context = context;//recibimos el contexto
        enviarNotificacion(nombres);
    }

    //Función para enviar la notificación
    public void enviarNotificacion(List<String> nombres) {

        String mensaje = context.getString(R.string.saludoCumple);

        int notifId = 1; //Identificador de la notificación, para futuras modificaciones.

          /* PASO 1: Crear la notificación con sus propiedades */
        NotificationCompat.Builder constructorNotif = new NotificationCompat.Builder(context);
        constructorNotif.setSmallIcon(R.drawable.ic_stat_name);
        constructorNotif.setContentTitle(context.getString(R.string.tituloCumple));
        constructorNotif.setContentText(mensaje);

         /* PASO 2: Creamos un intent para abrir la actividad cuando se pulse la notificación*/
        Intent resultadoIntent = new Intent(context, MainActivity.class);
        //El objeto stackBuilder crea un back stack que nos asegura que el botón de "Atrás" del
        // dispositivo nos lleva desde la Actividad a la pantalla principal
        TaskStackBuilder pila = TaskStackBuilder.create(context);
        // El padre del stack será la actividad a crear
        pila.addParentStack(MainActivity.class);
        // Añade el Intent que comienza la Actividad al inicio de la pila
        pila.addNextIntent(resultadoIntent);
        PendingIntent resultadoPendingIntent = pila.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        constructorNotif.setContentIntent(resultadoPendingIntent);



        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        // Título del expanded layout
        inboxStyle.setBigContentTitle(mensaje);
        String[] arrayNombres = new String[nombres.size()];

        for (String nombre : nombres) {
            inboxStyle.addLine(nombre);
        }

        /* dar la máxima prioridad y ponerlo en la cima de las notificaciones */
        constructorNotif.setWhen(0);
        constructorNotif.setPriority(Notification.PRIORITY_MAX);
        // Mueve el expanded layout a la notificación.
        constructorNotif.setStyle(inboxStyle);

        NotificationManager notificador = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificador.notify(notifId, constructorNotif.build());
    }
}

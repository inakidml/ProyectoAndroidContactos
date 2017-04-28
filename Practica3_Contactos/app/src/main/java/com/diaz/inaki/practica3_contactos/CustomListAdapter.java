package com.diaz.inaki.practica3_contactos;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 8fdi02 on 23/3/17.
 */

public class CustomListAdapter extends ArrayAdapter<Contacto> {

    private final Activity context;
    private List<Contacto> listaContactos;

    public CustomListAdapter(Activity context, List<Contacto> contactos) {

        super(context, R.layout.fila_lista, contactos);
        this.context = context;
        this.listaContactos = contactos;

    }

    public View getView(int posicion, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.fila_lista, null, true);

        ImageView imageViewPhoto = (ImageView) rowView.findViewById(R.id.photoVerContacto);
        TextView textViewNombre = (TextView) rowView.findViewById(R.id.textoNombre);
        TextView textViewNumero = (TextView) rowView.findViewById(R.id.textoNumero);
        TextView textViewAviso = (TextView) rowView.findViewById(R.id.textoAviso);

        textViewNombre.setText(listaContactos.get(posicion).getName());
        if (listaContactos.get(posicion).getTipoNotif() == 'y') {
            textViewAviso.setText("Aviso: Notificación y SMS");
        } else {
            textViewAviso.setText("Aviso: Solo notificación");
        }
        textViewNumero.setText(listaContactos.get(posicion).getFechaNacimiento() + "  t: " + listaContactos.get(posicion).getTelefono());
        imageViewPhoto.setImageURI(Uri.parse(listaContactos.get(posicion).getPhotoURI()));

        return rowView;
    }
}

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
        //debug
        if(MainActivity.DEBUG){
            System.out.println("Constructor customlistadapter");
        }
        this.context = context;
        this.listaContactos = contactos;

    }

    //por cada elemento del arraylist llama a esta función (solo los que haya visibles en pantalla)
    public View getView(int posicion, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.fila_lista, null, true);//inflamos con la plantilla que tenemos de la fila
        //referencias a elementos de la vista
        ImageView imageViewPhoto = (ImageView) rowView.findViewById(R.id.photoVerContacto);
        TextView textViewNombre = (TextView) rowView.findViewById(R.id.textoNombre);
        TextView textViewNumero = (TextView) rowView.findViewById(R.id.textoNumero);
        TextView textViewAviso = (TextView) rowView.findViewById(R.id.textoAviso);
        //tipo de aviso
        textViewNombre.setText(listaContactos.get(posicion).getName());
        if (listaContactos.get(posicion).getTipoNotif() == 'y') {
            textViewAviso.setText(R.string.notysms);
        } else {
            textViewAviso.setText(R.string.not);
        }

        textViewNumero.setText(listaContactos.get(posicion).getFechaNacimiento() + "  t: " + listaContactos.get(posicion).getTelefono());
        //foto
        if (!listaContactos.get(posicion).getPhotoURI().toString().equals(context.getString(R.string.vacio))){
            imageViewPhoto.setImageURI(Uri.parse(listaContactos.get(posicion).getPhotoURI()));
        }else{imageViewPhoto.setImageResource(R.drawable.ic_action_name);}

        //debug
        if(MainActivity.DEBUG) {
            System.out.println("Fila " + posicion + " de " + listaContactos.size());
        }
        //devolvemos la fila
        return rowView;

    }
}

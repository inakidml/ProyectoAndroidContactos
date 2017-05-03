package com.diaz.inaki.practica3_contactos;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.SQLOutput;

public class VerContactoActivity extends AppCompatActivity implements View.OnClickListener {
    private Contacto contactoOriginal;
    private Contacto c;
    private CheckBox aviso;//ref a aviso
    private EditText mensaje;//ref a mensaje
    private static final int CONTACT_REQUEST = 2; //id para intent a app de contactos

    private Intent intentVuelta; //intent de vuelta a MainActivity


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_contacto);
        //referencia a los botones de la vista y onClicklistener
        Button botonGuardar = (Button) findViewById(R.id.botonGuardar);
        botonGuardar.setOnClickListener(this);
        Button botonDetalles = (Button) findViewById(R.id.botonDetalles);
        botonDetalles.setOnClickListener(this);
        //recuperamos el contacto que nosllega en el intent
        contactoOriginal = (Contacto) getIntent().getSerializableExtra("Contacto");
        //Clonamos el contacto para devolver original y modificado con el fin de saber si ha habido cambios
        c = new Contacto();
        c.setName(contactoOriginal.getName());
        c.setID(contactoOriginal.getID());
        c.setFechaNacimiento(contactoOriginal.getFechaNacimiento());
        c.setPhotoURI(contactoOriginal.getPhotoURI());
        c.setMensaje(contactoOriginal.getMensaje());
        c.setTelefono(contactoOriginal.getTelefono());
        c.setTipoNotif(contactoOriginal.getTipoNotif());
        //rellenamos la vista con los datos
        rellenarFicha();
        //preparamos un intent de vuelta
        //si dan para atrás por lo menos sabemos los contactos
        intentVuelta = new Intent();
        intentVuelta.putExtra("Contacto Original", contactoOriginal);
        intentVuelta.putExtra("Contacto Modificado", c);
        setResult(RESULT_CANCELED, intentVuelta);

    }

    @Override
    public void onClick(View view) {//botones

        switch (view.getId()) {
            case R.id.botonGuardar:
                c.setMensaje(leerMensaje(mensaje));//cogemos el mensaje
                c.setTipoNotifBoolean(aviso.isChecked());//y el aviso
                intentVuelta = new Intent();
                intentVuelta.putExtra("Contacto Original", contactoOriginal);
                intentVuelta.putExtra("Contacto Modificado", c);
                setResult(RESULT_OK, intentVuelta);
                finish();
                break;
            case R.id.botonDetalles:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(c.getID()));
                intent.setData(uri);
                startActivityForResult(intent, CONTACT_REQUEST);
                break;
            default:
                System.out.println(R.string.noBoton);
        }

    }


    private void rellenarFicha() {

        //referencia a los elementos para rellenar de la vista
        ImageView photo = (ImageView) findViewById(R.id.photoVerContacto);
        TextView nombre = (TextView) findViewById(R.id.textoNombreVerContacto);
        TextView telefono = (TextView) findViewById(R.id.textoTelefonoVerContacto);
        aviso = (CheckBox) findViewById(R.id.checkBoxVerContacto);
        TextView fechaNacimiento = (TextView) findViewById(R.id.fechaNacimientoVerContacto);
        mensaje = (EditText) findViewById(R.id.mensajeVerContacto);
        //si la foto no esta vacía, la cargamos
        if (!c.getPhotoURI().toString().equals(getString(R.string.vacio))) {
            photo.setImageURI(Uri.parse(c.getPhotoURI()));
        }else{photo.setImageResource(R.drawable.ic_action_name);}
        //cargamos el resto de elementos
        nombre.setText(c.getName());
        telefono.setText(c.getTelefono());
        fechaNacimiento.setText(c.getFechaNacimiento());
        mensaje.setHint(c.getMensaje());
        if (c.getTipoNotif() == 'y') {
            aviso.setChecked(true);
        }
    }

    //Función para leer el mensaje
    //si no hay texto escrito devuelve el hint (placeholder)
    private String leerMensaje(EditText mensaje) {
        return (mensaje.getText().toString().matches("") ? mensaje.getHint().toString() : mensaje.getText().toString());

    }

    //resultado del intent a app contactos
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //https://developer.android.com/training/basics/intents/result.html
        // Check which request we're responding to
        if (requestCode == CONTACT_REQUEST) {
            //siempre se vuelve dandole a la flecha hacia atrás, así que el result==CANCELED

            //por si ha modificado el contacto, lo refrescamos
            actualizarContacto();

            //preparamos un intent de vuelta por si vuelve con la flecha para atrás
            intentVuelta = new Intent();
            intentVuelta.putExtra("Contacto Original", contactoOriginal);
            intentVuelta.putExtra("Contacto Modificado", c);
            setResult(RESULT_CANCELED, intentVuelta);

            // Make sure the request was successful
            /*Al volver dandole a la flecha para atrás, el resultado es RESULT_CANCELLED (0)
            //System.out.println(resultCode);
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
            }*/


        }

    }

    //actualizar info del contacto
    private void actualizarContacto() {
        //Conseguir nombre, ID y foto
        String proyeccion[] = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_URI};

        String filtro = ContactsContract.Contacts._ID + " = " + c.getID();
        String args_filtro[] = {"%" + "" + "%"};

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, proyeccion, filtro, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                String imageURI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                //Conseguir cumple
                String bDay = conseguirCumple(id);
                //conseguir el número Movil
                String telefono = conseguirMovil(id);


                c.setID(id);
                c.setName(name);
                c.setTelefono(telefono);
                //si no tiene imagen, ponemos vacío, si no, al hacer equals de null , falla
                if (imageURI == null) {
                    imageURI = getString(R.string.vacio);
                }
                c.setPhotoURI(imageURI);
                c.setFechaNacimiento(bDay);

            }
        }

        rellenarFicha();
    }

    private String conseguirMovil(String id) {

        //Cursor para conseguir solo el número de movil
        Uri uriPhones = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String wherePhones = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id;
        ContentResolver cr = getContentResolver();
        Cursor phones = cr.query(uriPhones, null, wherePhones, null, null);

        String telefono = getString(R.string.vacio); // variable temporal para el numero

        while (phones.moveToNext()) {
            String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            switch (type) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    telefono = number;
                    //si no hay movil probamos con los demas
                    break;

                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    if (telefono == getString(R.string.vacio)) {
                        telefono = number;
                    }
                    //break;

                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    if (telefono == getString(R.string.vacio)) {
                        telefono = number;
                    }
                    //break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                    if (telefono == getString(R.string.vacio)) {
                        telefono = number;
                    }
                    break;

            }
        }
        phones.close();
        return telefono;
    }

    private String conseguirCumple(String id) {
        //Conseguir Fecha nacimiento

        //http://stackoverflow.com/questions/8579883/get-birthday-for-each-contact-in-android-application/8638744

        //preparamos cursor filtrando con el ID del contacto
        //ojo URI de Data, no de Contacts
        //tabla de data
        Uri uri = ContactsContract.Data.CONTENT_URI;

        String[] projection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.START_DATE};

        //filtramos por id de contacto y tipo de fecha, cumpleaños
        String where =
                ContactsContract.Data.MIMETYPE + "= ? AND " +
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id + " AND " +
                        ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
        String[] selectionArgs = new String[]{
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE};

        String sortOrder = null;

        Cursor birthCursor = getContentResolver().query(uri, projection, where, selectionArgs, sortOrder);
        //¿Cual es la columna de la fecha?
        int bDayColumn = birthCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);

        String bDay = getString(R.string.vacio);
        while (birthCursor.moveToNext()) {
            //conseguimos la fecha
            bDay = birthCursor.getString(bDayColumn);
        }
        //cerramos cursor
        birthCursor.close();

        return bDay;
    }


}




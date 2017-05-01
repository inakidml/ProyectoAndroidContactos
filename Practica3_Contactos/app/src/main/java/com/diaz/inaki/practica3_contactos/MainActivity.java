package com.diaz.inaki.practica3_contactos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TimePicker;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private Modelo mod;
    private static final int INTENTPARAVERCONTACTO = 1;
    private ListView l;
    private Menu refMenu; //referencia para poder cambiar el icono del menú
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent; //para el broadcast de la alarma
    private int horaAlarma = 12; // hora por defecto para la alarma
    private int minutoAlarma = 00;

    private int curSize = 100;
    private int curPos = 0;

    public static Boolean DEBUG = true; // constante para hacer debug


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mod = new Modelo(this); //Instanciamos un modelo para poder guardar
        //en el constructor carga la BD en array
        Alarma.mod = mod; //pasamos la referencia del modelo (statica) a la clase alarma, ya que puede ser llamada por un broadcast
        setAlarma(horaAlarma, minutoAlarma); //activamos la alarma con la hora por defecto
        mod.ordenarArrays();
        cargarListView();//Vista previa de contactos
        final Thread t = new Thread() {//lo mandamos a un hilo para poder hacer progressBar
            @Override
            public void run() {
                rellenarListaContactosDesdeTel(); //lee los contactos del movil y rellena la BD y arrays

                runOnUiThread(new Runnable() {//para poder ejecuarlo en el hilo principal
                    @Override
                    public void run() {
                        mod.limpiarDB();//borramos los contactos que ya no estan en el teléfono
                        mod.ordenarArrays();//ordenamos los arrays para que aparezcan en orden alfabético
                        cargarListView();//rellena el listview
                    }
                });
            }
        };
        t.start();
    }

    //inflamos el menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflar Menú
        //http://www.sgoliver.net/blog/menus-en-android-i-conceptos-basicos/

        getMenuInflater().inflate(R.menu.menu, menu);
        refMenu = menu;
        return true;
    }

    //opciones del menú
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {//por si metemos mas opciones
            case R.id.MnuOpc1://Seleccionar hora

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(this, 0, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        horaAlarma = selectedHour;
                        minutoAlarma = selectedMinute;
                        setAlarma(horaAlarma, minutoAlarma);//cambiamos la hora de la alarma

                    }
                }, hour, minute, true);
                mTimePicker.setTitle(R.string.selectorHora);
                mTimePicker.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //rellena la base de datos con los contactos del teléfono
    public void rellenarListaContactosDesdeTel() {
        //debug
        if (MainActivity.DEBUG) {
            System.out.println("MainActivuty rellenar contactos desde tel ");
        }

        //Conseguir nombre, ID y foto desde el teléfono
        String proyeccion[] = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_URI};

        String filtro = ContactsContract.Contacts.DISPLAY_NAME + " like ?";//las interrogaciones se sustituyen por los args_filtro
        String args_filtro[] = {"%" + "" + "%"};

        ContentResolver cr = getContentResolver();//content resolver
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, proyeccion, filtro, args_filtro, null); //cursor de la query

        curSize = cur.getCount();//para la progress bar

        runOnUiThread(new Runnable() {//mandamos el progressBar al hilo principal
            @Override
            public void run() {
                progresoRellenar();
            }
        });

        if (cur.getCount() > 0) {//si el cursor tiene datos
            while (cur.moveToNext()) {

                curPos = cur.getPosition();

                //debug
                /*if (DEBUG) {
                    System.out.println("siguiente del cursor");
                }*/
                //vamos sacando la información
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String imageURI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                //Conseguir cumple
                String bDay = conseguirCumple(id);
                //conseguir el número Movil
                String telefono = conseguirMovil(id);

                //Contacto temporal
                Contacto c = new Contacto();
                c.setID(id);
                c.setName(name);
                c.setTipoNotifBoolean(false);
                c.setTelefono(telefono);
                //si no tiene imagen, ponemos vacío, si no, al hacer equals de null , falla
                if (imageURI == null) {
                    imageURI = getString(R.string.vacio);
                }
                c.setPhotoURI(imageURI);
                c.setFechaNacimiento(bDay);
                c.setMensaje(getString(R.string.vacio));
                //lo añadimos a una lista para saber que contactos siguen en el teléfono para luego poder limpiar
                mod.getListaIdsPhone().put(id, c);

                //si este id existe ya, lo vamos a comparar, para saber si se ha modificado, else lo añadimos directamente
                if (mod.getListaIdsBd().containsKey(c.getID())) {

                    //debug
                    /*if (DEBUG) {
                        System.out.println("contiene la clave");
                    }*/
                    //conseguimos el contacto con el mismo id
                    Contacto cGuardado = mod.getListaContactos().get(mod.getListaIdsBd().get(c.getID()));

                    if (!cGuardado.equals(c)) {//si no es igual lo modificamos
                        //debug
                        if (DEBUG) {
                            System.out.println("Contacto difiere");
                            System.out.println(cGuardado);
                            System.out.println(c);
                        }
                        //Si el contacto se ha modificado, lo cambiamos
                        //mantenemos el mensaje y el tipo de notificación
                        String mensaje = cGuardado.getMensaje();
                        char aviso = cGuardado.getTipoNotif();
                        c.setMensaje(mensaje);
                        c.setTipoNotif(aviso);
                        //cambiamos el contacto a traves del modelo
                        mod.modificarContactoDB(c, cGuardado);
                    }//si es igual, no hacemos nada
                }//si no existe el id lo añadimos directamente
                else {
                    mod.aniadirContactoDB(c);
                }
            }
        }
        cur.close();//Cerramos el cursor

        //debug
        if (MainActivity.DEBUG) {
            System.out.println("cursor cerrado");
        }

        if (MainActivity.DEBUG) {
            System.out.println("fin rellenar");
        }
    }

    //barra de preogreso para rellenarcontactosdesdetel
    private void progresoRellenar(){

        final ProgressDialog progress = new ProgressDialog(this);;
        progress.setMessage("Buscando cambios en contactos");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(curSize);
        progress.setProgress(0);
        progress.show();

        final Thread t = new Thread() {
            @Override
            public void run() {
                while (curPos<curSize-1){
                    progress.setProgress(curPos+1);
                }
                progress.dismiss();
            }
        };
        t.start();

    }
    //recibe los resultados de la actividad ver detalle
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INTENTPARAVERCONTACTO) {

            if (resultCode == RESULT_OK) {//si se da al botón guardar el result es OK
                Contacto contactoOriginal = (Contacto) data.getSerializableExtra("Contacto Original");
                Contacto contactoModificado = (Contacto) data.getSerializableExtra("Contacto Modificado");
                mod.modificarContactoDB(contactoModificado, contactoOriginal);
                l.setAdapter(new CustomListAdapter(this, mod.getListaContactos()));

            } else if (resultCode == RESULT_CANCELED) {//si le dan a flecha para atrás
                Contacto contactoOriginal = (Contacto) data.getSerializableExtra("Contacto Original");
                Contacto contactoModificado = (Contacto) data.getSerializableExtra("Contacto Modificado");
                if (!contactoOriginal.equals(contactoModificado)) {
                    String mensaje = contactoOriginal.getMensaje();
                    char aviso = contactoOriginal.getTipoNotif();
                    contactoModificado.setMensaje(mensaje);
                    contactoModificado.setTipoNotif(aviso);
                    mod.modificarContactoDB(contactoModificado, contactoOriginal);
                    l.setAdapter(new CustomListAdapter(this, mod.getListaContactos()));
                }
            }
        }
    }
    //función para conseguir el número de un contacto, pasandole el id
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
    //función para conseguir fecha de cumpleaños de un contacto, pasandole el id
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
    //Alarma para las notificaciones y sms
    public void setAlarma(int hora, int minutos) {
        if (DEBUG) {
            System.out.println("Activar alarma a las : " + hora + minutos);
        }

        if (alarmMgr == null) {//si no se ha instanciado ya

       /*configurar calendario*/
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hora);
            calendar.set(Calendar.MINUTE, minutos);
       /*crear la alarma*/
            Intent intent = new Intent(this, Alarma.class);
            alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0); //pending intent, no va a ser ahora
            alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);//repetición todos los días
            if (DEBUG) {
                System.out.println("Alarma configurada a las " + hora + ":" + minutos);
            }
        } else { //si ya estaba activada, cambiamos la hora
            /*configurar calendario*/
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hora);
            calendar.set(Calendar.MINUTE, minutos);
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
            if (DEBUG) {
                System.out.println("Alarma cambiada" + hora + minutos);
            }
        }
    }
    //cargar el listview
    private void cargarListView() {

        //Mandamos el adaptador a la lista de contactos
        //  http://www.codigojavalibre.com/2015/10/crear-un-listview-con-imagenes-en-Android-Studio.html

        l = (ListView) findViewById(R.id.listViewContactos);//referencia al listview
        l.setAdapter(new CustomListAdapter(this, mod.getListaContactos()));//le pasamos el adaptador con el array de contactos

        //añadimos onitemclicklistener al listview con un intent a la actividad vercontacto
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, VerContactoActivity.class);
                Contacto c = mod.getListaContactos().get(i);
                intent.putExtra("Contacto", c);
                startActivityForResult(intent, INTENTPARAVERCONTACTO);
            }
        });
    }
}



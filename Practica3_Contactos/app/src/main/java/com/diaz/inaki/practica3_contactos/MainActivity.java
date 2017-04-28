package com.diaz.inaki.practica3_contactos;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
    private int horaAlarma = 20;
    private int minutoAlarma = 00;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;
        mod = new Modelo(this);
        Alarma.mod = mod;
        System.out.println("onCreate main");
        //mod.OJOborrarDB();
        setAlarma(horaAlarma, minutoAlarma);
        rellenarListaContactosDesdeTel();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflar Menú
        //http://www.sgoliver.net/blog/menus-en-android-i-conceptos-basicos/

        getMenuInflater().inflate(R.menu.menu, menu);
        refMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                        setAlarma(horaAlarma, minutoAlarma);

                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Seleccionar hora notificación");
                mTimePicker.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void rellenarListaContactosDesdeTel() {
        //Conseguir nombre, ID y foto
        String proyeccion[] = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_URI};

        String filtro = ContactsContract.Contacts.DISPLAY_NAME + " like ?";//las interrogaciones se sustituyen por los args_filtro
        String args_filtro[] = {"%" + "" + "%"};

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, proyeccion, filtro, args_filtro, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String imageURI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                //Conseguir cumple
                String bDay = conseguirCumple(id);
                //conseguir el número Movil
                String telefono = conseguirMovil(id);

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Contacto c = new Contacto();
                    c.setID(id);
                    c.setName(name);
                    c.setTipoNotifBoolean(false);
                    c.setTelefono(telefono);
                    c.setPhotoURI(imageURI);
                    c.setFechaNacimiento(bDay);
                    c.setMensaje("Vacío");

                    if (mod.getListaIdsBd().containsKey(c.getID())) {
                        Contacto cGuardado = mod.getListaContactos().get(mod.getListaIdsBd().get(c.getID()));
                        if (!cGuardado.equals(c)) {
                            //Si el contacto se ha modificado, lo cambiamos
                            mod.modificarContactoDB(c, cGuardado);
                        }
                    } else {
                        mod.aniadirContactoDB(c);
                    }
                    //TODO comprobar que no se ha borrado algun contacto del teléfono, si es que sí, borrarlo
                }
            }
        }
        cur.close();//Cerramos el cursor

        //Mandamos el adaptador a la lista de contactos
        //  http://www.codigojavalibre.com/2015/10/crear-un-listview-con-imagenes-en-Android-Studio.html
        l = (ListView) findViewById(R.id.listaContactos);
        l.setAdapter(new CustomListAdapter(this, mod.getListaContactos()));
        //añadimos onitemclicklistener al listview
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

    //recibe los resultados de la actividad ver detalle
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INTENTPARAVERCONTACTO) {


            if (resultCode == RESULT_OK) {
                Contacto contactoOriginal = (Contacto) data.getSerializableExtra("Contacto Original");
                Contacto contactoModificado = (Contacto) data.getSerializableExtra("Contacto Modificado");
                mod.modificarContactoDB(contactoModificado, contactoOriginal);
                l.setAdapter(new CustomListAdapter(this, mod.getListaContactos()));

                // System.out.println("intent de vuelta OK");
            } else {
                //TODO si se da a la flecha para atrás, comprobar que no se ha modificado en el teléfono
            }
        }
    }

    private String conseguirMovil(String id) {

        //Cursor para conseguir solo el número de movil
        Uri uriPhones = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String wherePhones = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id;
        ContentResolver cr = getContentResolver();
        Cursor phones = cr.query(uriPhones, null, wherePhones, null, null);

        String telefono = "vacío"; // variable temporal para el numero

        while (phones.moveToNext()) {
            String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            switch (type) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    //de momento no lo usamos
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    telefono = number;
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    //de momento no lo usamos
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

        String bDay = "vacío";
        while (birthCursor.moveToNext()) {
            //conseguimos la fecha
            bDay = birthCursor.getString(bDayColumn);
        }
        //cerramos cursor
        birthCursor.close();

        return bDay;
    }

    public void setAlarma(int hora, int minutos) {
        //System.out.println("Activar alarma a las : " +  hora + minutos);
        //System.out.println(alarmMgr);

        if (alarmMgr == null) {

       /*configurar calendario*/
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hora);
            calendar.set(Calendar.MINUTE, minutos);
       /*crear la alarma*/
            Intent intent = new Intent(this, Alarma.class);
            alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
            //System.out.println("Alarma configurada a las " + hora + ":" + minutos);
        } else {
            /*configurar calendario*/
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hora);
            calendar.set(Calendar.MINUTE, minutos);
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
            //System.out.println("Alarma cambiada" + hora + minutos);

        }
    }
}



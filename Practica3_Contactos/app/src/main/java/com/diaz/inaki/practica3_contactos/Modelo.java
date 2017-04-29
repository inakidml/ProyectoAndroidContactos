package com.diaz.inaki.practica3_contactos;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;


/**
 * Created by 8fdi02 on 30/3/17.
 */

public class Modelo {
    private SQLiteDatabase db;
    private Context c;
    private List<Contacto> listaContactos;
    private Map<String, Integer> listaIdsBd; //lista para saber cuales estan en la BD y su posición en el arraylist


    public Modelo(Context c) {
        this.c = c;
        listaContactos = new ArrayList<Contacto>();
        listaIdsBd = new HashMap<>();
        crearTabla();
        cargarBD();
        listarDB();

    }

    private void abrirDB() {
        db = c.openOrCreateDatabase("MisCumples", Context.MODE_PRIVATE, null);

    }

    private void cerrarDB() {
        db.close();
    }

    private void crearTabla() {
        abrirDB();
        db.execSQL("CREATE TABLE IF NOT EXISTS misCumples(ID integer, TipoNotif char(1), Mensaje VARCHAR(160), Telefono VARCHAR(15), FechaNacimiento VARCHAR(15), Nombre VARCHAR(128), URIPhoto VARCHAR(128));");
        cerrarDB();
    }


    //Añadir contacto a DB
    public void aniadirContactoDB(Contacto c) {
        abrirDB();
        db.execSQL("INSERT INTO misCumples VALUES('" + c.getID() + "', '" + c.getTipoNotif() + "', '" + c.getMensaje() + "','" + c.getTelefono() + "','" + c.getFechaNacimiento() + "','" + c.getName() + "','" + c.getPhotoURI() + "')");

        listaContactos.add(c);
        listaIdsBd.put(c.getID(), listaContactos.indexOf(c));
        cerrarDB();
        //System.out.println("Contacto añadido " + c);
    }

    //Cargar contactos de BD en arraylist
    public void cargarBD() {
        abrirDB();
        Cursor c = db.rawQuery("SELECT * FROM misCumples", null);
        if (c.getCount() == 0)
            System.out.println(R.string.noRegistros);
        else {
            listaIdsBd.clear(); //borramos
            while (c.moveToNext()) {

                Contacto contactoTemp = new Contacto();

                contactoTemp.setID(c.getString(0));
                contactoTemp.setTipoNotif(c.getString(1).charAt(0));
                contactoTemp.setMensaje(c.getString(2));
                contactoTemp.setTelefono(c.getString(3));
                contactoTemp.setFechaNacimiento(c.getString(4));
                contactoTemp.setName(c.getString(5));
                contactoTemp.setPhotoURI(c.getString(6));

                listaContactos.add(contactoTemp); // arraylist de contactos
                getListaIdsBd().put(c.getString(0), listaContactos.indexOf(contactoTemp));//lo añadimos al map para saber si esta

            }
        }
        cerrarDB();
    }
    //Ordenar arrays alfabeticamente para que salgan ordenados en el listview
    public void ordenarArrays() {

        Collections.sort(listaContactos, new Comparator<Contacto>() {
            @Override
            public int compare(Contacto o1, Contacto o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        listaIdsBd.clear(); //borramos
        for (int i = 0; i < listaContactos.size(); i++) {
            Contacto c = listaContactos.get(i);
            getListaIdsBd().put(c.getID(), i);//lo añadimos al map para saber si esta
        }


    }

    public void listarDB() {
        abrirDB();
        Cursor c = db.rawQuery("SELECT * FROM misCumples", null);
        if (c.getCount() == 0)
            System.out.println(R.string.noRegistros);
        else {
            while (c.moveToNext()) {
                System.out.println(c.getString(0) + "-" + c.getString(1) + "-" + c.getString(2) + "-" + c.getString(3) + "-" + c.getString(4) + "-" + c.getString(5) + "-" + c.getString(6));
            }
        }
        cerrarDB();
    }

    //Borrar contacto de BD
    public void borrarContactoDB(Contacto c) {
        db.execSQL("DELETE FROM misCumples WHERE ID = '" + c.getID() + "'");
        listaIdsBd.remove(c.getID());
    }

    //Borrar todas las entradas de la BD
    public void OJOborrarDB() {
        abrirDB();
        db.execSQL("DELETE FROM misCumples");
        cerrarDB();
    }

    public void modificarContactoDB(Contacto c, Contacto cGuardado) {
        abrirDB();
        String id = c.getID();
        db.execSQL("UPDATE misCumples SET TipoNotif ='" + c.getTipoNotif() + "', Mensaje ='" + c.getMensaje() + "', Telefono = '" + c.getTelefono() + "', FechaNacimiento = '" + c.getFechaNacimiento() + "', Nombre = '" + c.getName() + "', URIPhoto = '" + c.getPhotoURI() + "' WHERE ID = '" + id + "'");
        cerrarDB();
        //sustituir en el arraylist
        getListaContactos().set(getListaIdsBd().get(cGuardado.getID()), c);
    }


    //Getters y Setters

    public List<Contacto> getListaContactos() {
        return listaContactos;
    }

    public Map<String, Integer> getListaIdsBd() {
        return listaIdsBd;
    }

}

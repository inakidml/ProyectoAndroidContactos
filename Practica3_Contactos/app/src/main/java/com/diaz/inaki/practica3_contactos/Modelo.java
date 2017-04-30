package com.diaz.inaki.practica3_contactos;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
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
    private Map<String, Contacto> listaIdsPhone; // lista de los contactos que hay en el telefono

    //Constructor
    public Modelo(Context c) {
        this.c = c;
        listaContactos = new ArrayList<Contacto>();
        listaIdsBd = new HashMap<>();
        listaIdsPhone = new HashMap<>();
        crearTabla(); //crea la tabla si no esta creada ya
        cargarDB(); //vuelca la BD a los arrays

        //debug
        if (MainActivity.DEBUG) {
            listarDB();
        }

    }

    private void abrirDB() {
        db = c.openOrCreateDatabase("MisCumples", Context.MODE_PRIVATE, null);

    }

    private void cerrarDB() {
        db.close();
    }

    //crea la tabla si no esta creada ya
    private void crearTabla() {
        abrirDB();
        db.execSQL("CREATE TABLE IF NOT EXISTS misCumples(ID integer, TipoNotif char(1), Mensaje VARCHAR(160), Telefono VARCHAR(15), FechaNacimiento VARCHAR(15), Nombre VARCHAR(128), URIPhoto VARCHAR(128));");
        cerrarDB();
    }


    //Añadir contacto a DB
    public void aniadirContactoDB(Contacto c) {
        //debug
        if (MainActivity.DEBUG) {
            System.out.println("Añadiendo contacto : " + c);
        }

        //Añadir contacto a DB
        abrirDB();
        db.execSQL("INSERT INTO misCumples VALUES('" + c.getID() + "', '" + c.getTipoNotif() + "', '" + c.getMensaje() + "','" + c.getTelefono() + "','" + c.getFechaNacimiento() + "','" + c.getName() + "','" + c.getPhotoURI() + "')");
        //Añadir contacto a arrays
        listaContactos.add(c);
        listaIdsBd.put(c.getID(), listaContactos.indexOf(c));
        cerrarDB();

        //debug
        if (MainActivity.DEBUG) {
            System.out.println("Contacto añadido " + c);
        }
    }

    //Cargar contactos de BD en arraylist
    public void cargarDB() {
        if (MainActivity.DEBUG) {
            System.out.println("cargando DB");
        }
        //Cargamos los arrays con los contactos de la BD
        abrirDB();
        Cursor c = db.rawQuery("SELECT * FROM misCumples", null);
        if (c.getCount() == 0)
            System.out.println(R.string.noRegistros);
        else {
            listaIdsBd.clear(); //borramos
            while (c.moveToNext()) {

                Contacto contactoTemp = new Contacto(); // contacto temporal

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

        //debug
        if (MainActivity.DEBUG) {
            System.out.println("DB cargada");
        }
    }

    //Ordenar arrays alfabeticamente para que salgan ordenados en el listview
    public void ordenarArrays() {

        if (MainActivity.DEBUG) {
            System.out.println("Ordenando arrays");
        }

        Collections.sort(
                listaContactos, new Comparator<Contacto>() {
                    @Override
                    public int compare(Contacto o1, Contacto o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                }
        );

        listaIdsBd.clear(); //borramos map para reordenar
        for (int i = 0; i < listaContactos.size(); i++) {
            Contacto c = listaContactos.get(i);
            getListaIdsBd().put(c.getID(), i);//lo añadimos al map para saber si esta
        }

        //debug
        if (MainActivity.DEBUG) {
            System.out.println("Arrays ordenados");
        }

    }

    //lista los contactos de la BD
    public void listarDB() {
        if (MainActivity.DEBUG) {
            System.out.println("Listando DB");
        }
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
        if (MainActivity.DEBUG) {
            System.out.println("Fin");
        }
    }

    //Borrar contacto de BD y arrays
    public void borrarContactoDB(Contacto c) {
        abrirDB();
        db.execSQL("DELETE FROM misCumples WHERE ID = '" + c.getID() + "'");
        int posicion = listaIdsBd.get(c.getID());
        listaIdsBd.remove(c.getID());
        listaContactos.remove(posicion);
        cerrarDB();

        //debug
        if (MainActivity.DEBUG) {
            System.out.println("Contacto borrado");
        }
    }

    //Borrar todas las entradas de la BD
    public void OJOborrarDB() {
        abrirDB();
        db.execSQL("DELETE FROM misCumples");
        cerrarDB();
    }

    //modifica un contacto por otro de BD y arrays
    public void modificarContactoDB(Contacto c, Contacto cGuardado) {
        //debug
        if (MainActivity.DEBUG) {
            System.out.println("modificando contacto ");
            System.out.println(cGuardado);
            System.out.println(" por ");
            System.out.println(c);
        }

        //modificamos contacto en DB
        abrirDB();
        String id = c.getID();
        db.execSQL("UPDATE misCumples SET TipoNotif ='" + c.getTipoNotif() + "', Mensaje ='" + c.getMensaje() + "', Telefono = '" + c.getTelefono() + "', FechaNacimiento = '" + c.getFechaNacimiento() + "', Nombre = '" + c.getName() + "', URIPhoto = '" + c.getPhotoURI() + "' WHERE ID = '" + id + "'");
        cerrarDB();
        //sustituir en el arraylist
        getListaContactos().set(getListaIdsBd().get(cGuardado.getID()), c);

        //debug
        if (MainActivity.DEBUG) {
            System.out.println("contacto modificado");
        }
    }

    public void limpiarDB(){
        //comprobación de contactos no existentes

        //Borramos los contactos que se han borrado del teléfono
        Collection<Integer> listaIds = new ArrayList<>();
        listaIds = getListaIdsBd().values(); //volcamos las claves a una colleccíon
        List<Integer> listaParaBorrar = new ArrayList<>(); //lista provisional por problemas de concurrecia e inmutabilidad de la colección, no puedo borrar dentro del foreach
        //bucle para saber si los ids de la bd estan en la lista de las del teléfono
        for (Integer i : listaIds
                ) {
            if (!getListaIdsPhone().containsKey(getListaContactos().get(i).getID())) {
                listaParaBorrar.add(i);//si no esta lo añadimos para borrar mas tarde
            }
        }
        //recorremos la lista para borrar
        for (Integer i : listaParaBorrar
                ) {
            //y borramos
            borrarContactoDB(getListaContactos().get(i));

            //debug
            if (MainActivity.DEBUG) {
                System.out.println("No tiene la id, procedemos a borrar contacto");
            }
        }

    }


    //Getters y Setters

    public List<Contacto> getListaContactos() {
        return listaContactos;
    }

    public Map<String, Integer> getListaIdsBd() {
        return listaIdsBd;
    }

    public Map<String, Contacto> getListaIdsPhone() {
        return listaIdsPhone;
    }

}

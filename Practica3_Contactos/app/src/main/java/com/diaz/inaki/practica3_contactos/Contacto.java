package com.diaz.inaki.practica3_contactos;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by 8fdi02 on 23/3/17.
 */

public class Contacto implements Serializable{
    private String ID;
    private char tipoNotif;
    private String mensaje;
    private String telefono;
    private String fechaNacimiento;
    private String name;
    private String photoURI;



    public Contacto() {
    }


    //equals y HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contacto contacto = (Contacto) o;

        if (!ID.equals(contacto.ID)) return false;
        if (telefono != null ? !telefono.equals(contacto.telefono) : contacto.telefono != null)
            return false;
        if (fechaNacimiento != null ? !fechaNacimiento.equals(contacto.fechaNacimiento) : contacto.fechaNacimiento != null)
            return false;
        if (name != null ? !name.equals(contacto.name) : contacto.name != null) return false;
        return photoURI != null ? photoURI.equals(contacto.photoURI) : contacto.photoURI == null;

    }

    @Override
    public int hashCode() {
        int result = ID.hashCode();
        result = 31 * result + (telefono != null ? telefono.hashCode() : 0);
        result = 31 * result + (fechaNacimiento != null ? fechaNacimiento.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (photoURI != null ? photoURI.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Contacto{" +
                "ID='" + ID + '\'' +
                ", tipoNotif=" + tipoNotif +
                ", mensaje='" + mensaje + '\'' +
                ", telefono='" + telefono + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                ", name='" + name + '\'' +
                ", photoURI='" + photoURI + '\'' +
                '}';
    }

    //Getters y Setters

    public String getPhotoURI() {
        return photoURI;
    }

    public void setPhotoURI(String photoURI) {
        this.photoURI = photoURI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public char getTipoNotif() {
        return tipoNotif;
    }

    public void setTipoNotif(char tipoNotif) {
        this.tipoNotif = tipoNotif;
    }

    public void setTipoNotifBoolean(Boolean checked) {
        if(checked){
            this.tipoNotif = 'y';
        }else{
            this.tipoNotif = 'n';
        }


    }



}

package es.ujaen.dae.eventosapi.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class Usuario implements Serializable {

    @Id
    String dni;
    String nombre;
    String correo;
    String telefono;
    String password;

    @OneToMany(mappedBy = "organizador", fetch = FetchType.LAZY)
    public List<Evento> eventosOrganizados;

    @ManyToMany(mappedBy = "listaEspera")
    public List<Evento> eventosEspera;

    @ManyToMany(mappedBy = "listaInvitados")
    public List<Evento> eventosInvitado;

    @Version
    private int versionu;

    public Usuario() {
        eventosOrganizados = new ArrayList<>();
        eventosEspera = new ArrayList<>();
        eventosInvitado = new ArrayList<>();
    }

    public Usuario(String dni, String nombre, String correo, String telefono, String password) {
        this.dni = dni;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.password = password;
        eventosOrganizados = new ArrayList<>();
        eventosEspera = new ArrayList<>();
        eventosInvitado = new ArrayList<>();
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Evento> getEventosOrganizados() {
        return eventosOrganizados;
    }

    public void setEventosOrganizados(List<Evento> eventosOrganizados) {
        this.eventosOrganizados = eventosOrganizados;
    }

    public List<Evento> getEventosEspera() {
        return eventosEspera;
    }

    public void setEventosEspera(List<Evento> eventosEspera) {
        this.eventosEspera = eventosEspera;
    }

    public List<Evento> getEventosInvitado() {
        return eventosInvitado;
    }

    public void setEventosInvitado(List<Evento> eventosInvitado) {
        this.eventosInvitado = eventosInvitado;
    }

    @Override
    public String toString() {
        return "Usuario [dni=" + dni + ", nombre=" + nombre + ", correo=" + correo + ", telefono=" + telefono
                + ", password=" + password + ", eventosOrganizados=" + eventosOrganizados + ", eventosEspera="
                + eventosEspera + ", eventosInvitado=" + eventosInvitado + "]";
    }

}

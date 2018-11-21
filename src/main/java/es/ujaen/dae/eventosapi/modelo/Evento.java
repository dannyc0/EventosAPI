package es.ujaen.dae.eventosapi.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Evento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String nombre;
    String descripcion;
    String lugar;
    String fecha;
    String tipo;
    int cupo;

    @ManyToOne
    @JoinColumn(name = "organizador")
    public Usuario organizador;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "evento_espera", joinColumns = @JoinColumn(name = "evento_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "espera_dni", referencedColumnName = "dni"))
    public Map<Date, Usuario> listaEspera;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "evento_invitado", joinColumns = @JoinColumn(name = "evento_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "invitado_dni", referencedColumnName = "dni"))
    public List<Usuario> listaInvitados;

    @Version
    private int version;

    public Evento() {
        listaEspera = new TreeMap<>();
        listaInvitados = new ArrayList<>();
    }

    public Evento(String nombre, String descripcion, String lugar, String fecha, String tipo, int cupo,
            Usuario organizador) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.fecha = fecha;
        this.tipo = tipo;
        this.cupo = cupo;
        this.organizador = organizador;
        listaEspera = new TreeMap<>();
        listaInvitados = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public Usuario getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Usuario organizador) {
        this.organizador = organizador;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCupo() {
        return cupo;
    }

    public void setCupo(int cupo) {
        this.cupo = cupo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Map<Date, Usuario> getListaEspera() {
        return listaEspera;
    }

    public void setListaEspera(Map<Date, Usuario> listaEspera) {
        this.listaEspera = listaEspera;
    }

    public List<Usuario> getListaInvitados() {
        return listaInvitados;
    }

    public void setListaInvitados(List<Usuario> listaInvitados) {
        this.listaInvitados = listaInvitados;
    }

    @Override
    public String toString() {
        return "Evento [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + ", lugar=" + lugar
                + ", fecha=" + fecha + ", tipo=" + tipo + ", cupo=" + cupo + ", organizador=" + organizador
                + ", listaEspera=" + listaEspera + ", listaInvitados=" + listaInvitados + "]";
    }

    public boolean compararConFechaActual() {
        LocalDate hoy = LocalDate.now();
        String[] fechaEvento = getFecha().split("-");

        String format = hoy + "";
        String fechaActual[] = format.split("-");

        if (Integer.parseInt(fechaEvento[2]) > Integer.parseInt(fechaActual[0])) {
            return true;
        } else if (Integer.parseInt(fechaEvento[2]) == Integer.parseInt(fechaActual[0])) {
            if (Integer.parseInt(fechaEvento[1]) > Integer.parseInt(fechaActual[1])) {
                return true;
            } else if (Integer.parseInt(fechaEvento[1]) == Integer.parseInt(fechaActual[1])) {
                if (Integer.parseInt(fechaEvento[0]) > Integer.parseInt(fechaActual[2])) {
                    return true;
                }
            }
        }
        return false;
    }
}

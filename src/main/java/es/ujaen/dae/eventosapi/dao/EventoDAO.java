package es.ujaen.dae.eventosapi.dao;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.ujaen.dae.eventosapi.exception.ErrorCreacionEvento;
import es.ujaen.dae.eventosapi.modelo.Evento;
import es.ujaen.dae.eventosapi.modelo.Usuario;

import javax.persistence.LockModeType;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
@CacheConfig(cacheNames = "cacheEvento")
public class EventoDAO {

    @PersistenceContext
    EntityManager em;

    public EventoDAO() {
    }

    // Buscar evento
    @Cacheable
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Evento buscar(int id) {
        return em.find(Evento.class, id);
    }

    // Buscar evento
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Evento> buscarEventoPorTipo(String tipo) {
        List<Evento> eventosBuscados = em
                .createQuery("SELECT e FROM Evento e WHERE e.tipo = :tipo", Evento.class)
                .setParameter("tipo", tipo).getResultList();

        return eventosBuscados;
    }
    
    // Buscar evento
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Evento> buscarEventoPorDescripcion(String desc) {
        List<Evento> eventosBuscados = em
                .createQuery("SELECT e FROM Evento e WHERE e.descripcion LIKE :desc", Evento.class)
                .setParameter("desc", "%" + desc + "%").getResultList();

        return eventosBuscados;
    }

    // Obtener organizador del evento
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Usuario obtenerOrganizadorEvento(int id) {
        // Transaccion activa
        Evento evento = em.find(Evento.class, id);
        Usuario organizador = evento.getOrganizador();
        return organizador;
    }

    // Crear evento
    public void insertar(Evento evento) {
        try {
            em.persist(evento);
            em.flush();
        } catch (Exception e) {
        	System.out.println(e.getMessage());

        	System.out.println(evento.toString());
            throw new ErrorCreacionEvento();
        }
    }

    // Borrar evento
    public void borrar(Evento evento) {
        em.remove(em.merge(evento));
    }

    // Inscribir lista invitado
    public void inscribirInvitado(Evento evento, Usuario usuario) {
        // Transaccion activa, si se necesitabuscar
        Evento eventoInscribir = em.find(Evento.class, evento.getId());
        Usuario usuarioInscribir = em.find(Usuario.class, usuario.getDni());
        em.lock(eventoInscribir, LockModeType.OPTIMISTIC);
        em.lock(usuarioInscribir, LockModeType.OPTIMISTIC);
        eventoInscribir.getListaInvitados().add(usuarioInscribir);
    }

    // Cancelar de lista invitado
    public void cancelarInvitado(Evento evento, Usuario usuario) {
        // Transaccion activa, si se necesitabuscar

        Evento eventoInscribir = em.find(Evento.class, evento.getId());
        Usuario usuarioInscribir = em.find(Usuario.class, usuario.getDni());
        em.lock(eventoInscribir, LockModeType.OPTIMISTIC);
        em.lock(usuarioInscribir, LockModeType.OPTIMISTIC);
        eventoInscribir.getListaInvitados().remove(usuarioInscribir);

    }

    // Obtener quien es el siguiente de la lista de espera
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Object[] obtenerSiguienteDeListaEspera(Evento evento) {
        // Transaccion activa, si se necesitabuscar

        Evento eventoEspera = em.find(Evento.class, evento.getId());
        Object[] datosMap = null;
        try {
            datosMap = em.createQuery(
                    "SELECT KEY(map), VALUE(map).dni FROM Evento e INNER JOIN e.listaEspera map WHERE e.id = :id ORDER BY KEY(map) ASC",
                    Object[].class).setParameter("id", eventoEspera.getId()).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
           
        }

        return datosMap;
    }

    // Obtener datos de alguien que desea cancelar su inscripcion a la lista de
    // espera
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Object[] obtenerDatosListaEsperaParaCancelar(Evento evento, Usuario usuario) {
        // Transaccion activa
        Evento eventoEspera = em.find(Evento.class, evento.getId());
        Usuario usuarioEspera = em.find(Usuario.class, usuario.getDni());
        Object[] datosMap = null;
        try {
            datosMap = em.createQuery(
                    "SELECT KEY(map), VALUE(map).dni FROM Evento e INNER JOIN e.listaEspera map WHERE e.id = :id AND VALUE(map).dni = :dni",
                    Object[].class).setParameter("id", eventoEspera.getId()).setParameter("dni", usuarioEspera.getDni())
                    .setMaxResults(1).getSingleResult();

        } catch (Exception e) {
            
        }

        return datosMap;
    }

    // Sacarlo de la lista de espera
    public void sacarDeListaDeEspera(Object[] datosMap, Evento evento) {
        Evento eventoEspera = em.find(Evento.class, evento.getId());
        Usuario usuarioSacar = em.find(Usuario.class, datosMap[1]);
        em.lock(eventoEspera, LockModeType.OPTIMISTIC);
        em.lock(usuarioSacar, LockModeType.OPTIMISTIC);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(datosMap[0].toString().replaceAll("\\.\\d+", ""));
        } catch (ParseException e) {
        }

        eventoEspera.getListaEspera().remove(date, usuarioSacar);
    }

    // Inscribir lista espera
    public void inscribirEspera(Evento evento, Usuario usuario) {
        // Transaccion activa
        Evento eventoInscribir = em.find(Evento.class, evento.getId());
        Usuario usuarioInscribir = em.find(Usuario.class, usuario.getDni());
        em.lock(eventoInscribir, LockModeType.OPTIMISTIC);
        em.lock(usuarioInscribir, LockModeType.OPTIMISTIC);
        Calendar calendar = Calendar.getInstance();
        Timestamp fechaHoraActual = new Timestamp(calendar.getTime().getTime());

        eventoInscribir.getListaEspera().put(fechaHoraActual, usuarioInscribir);
    }

    // Validar invitado que no se repita en la lista
    @Cacheable
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean validarInvitadoLista(Evento evento, Usuario usuario) {
        // Transaccion activa

        Evento eventoInscribir = em.find(Evento.class, evento.getId());
        Usuario usuarioInscribir = em.find(Usuario.class, usuario.getDni());
        List<Evento> eventoBuscado = null;

        try {
            eventoBuscado = em
                    .createQuery(
                            "SELECT e FROM Evento e INNER JOIN e.listaInvitados l WHERE e.id = :id AND l.dni = :dni",
                            Evento.class)
                    .setParameter("id", eventoInscribir.getId()).setParameter("dni", usuarioInscribir.getDni())
                    .getResultList();
        } catch (Exception e) {
        }

        if (eventoBuscado.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    // Obtener tamaño actual de lista de invitados de un evento
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long obtenerSaturacion(Evento evento) {
        Evento eventoInscribir = em.find(Evento.class, evento.getId());

        long saturacion = (Long) em
                .createQuery("SELECT COUNT(e.id) FROM Evento e INNER JOIN e.listaInvitados l WHERE e.id = :id")
                .setParameter("id", eventoInscribir.getId()).getSingleResult();

        return saturacion;
    }

    // Lista de eventos en espera
    @Cacheable
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Evento> listarEventoEspera(Usuario usuario) {
        // Transaccion activa
        Usuario usuarioEspera = em.find(Usuario.class, usuario.getDni());
        usuarioEspera.getEventosEspera().size();
        return usuarioEspera.getEventosEspera();
    }

    // Lista de eventos inscritos
    @Cacheable
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Evento> listarEventoInscrito(Usuario usuario) {
        // Transaccion activa
        Usuario usuarioInscribir = em.find(Usuario.class, usuario.getDni());
        usuarioInscribir.getEventosInvitado().size();
        return usuarioInscribir.getEventosInvitado();
    }

    // Lista de eventos organizados
    @Cacheable
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Evento> listarEventoOrganizado(Usuario usuario) {
        // Transaccion activa
        Usuario organizador = em.find(Usuario.class, usuario.getDni());
        organizador.getEventosOrganizados().size();
        return organizador.getEventosOrganizados();
    }

}

package es.ujaen.dae.eventosapi.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.ujaen.dae.eventosapi.exception.ErrorCreacionEvento;
import es.ujaen.dae.eventosapi.modelo.Usuario;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class UsuarioDAO {

    @PersistenceContext
    EntityManager em;

    public UsuarioDAO() {
    }

    // Buscar evento
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Usuario buscar(String dni) {

        return em.find(Usuario.class, dni);
    }

    // Crear evento
    public void insertar(Usuario usuario) {
        try {
            em.persist(usuario);
            em.flush();
        } catch (Exception e) {
            throw new ErrorCreacionEvento();
        }
    }

    // Actualizar evento
    public void actualizar(Usuario usuario) {
        em.merge(usuario);
    }

    // Borrar evento
    public void borrar(Usuario usuario) {
        em.remove(em.merge(usuario));
    }

}

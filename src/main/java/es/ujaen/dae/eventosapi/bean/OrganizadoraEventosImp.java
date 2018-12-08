package es.ujaen.dae.eventosapi.bean;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.ujaen.dae.eventosapi.dao.EventoDAO;
import es.ujaen.dae.eventosapi.dao.UsuarioDAO;
import es.ujaen.dae.eventosapi.dto.EventoDTO;
import es.ujaen.dae.eventosapi.dto.UsuarioDTO;
import es.ujaen.dae.eventosapi.exception.CamposVaciosException;
import es.ujaen.dae.eventosapi.exception.CancelacionInvalidaException;
import es.ujaen.dae.eventosapi.exception.FechaInvalidaException;
import es.ujaen.dae.eventosapi.exception.InscripcionInvalidaException;
import es.ujaen.dae.eventosapi.exception.UsuarioNoRegistradoNoEncontradoException;
import es.ujaen.dae.eventosapi.modelo.Evento;
import es.ujaen.dae.eventosapi.modelo.Usuario;
import es.ujaen.dae.eventosapi.servicio.OrganizadoraEventosService;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Component
public class OrganizadoraEventosImp implements OrganizadoraEventosService {

    String cif;
    String nombre;

    Map<String, Usuario> usuarios;
    Map<Integer, Evento> eventos;

    @Autowired
    EventoDAO eventoDAO;

    @Autowired
    UsuarioDAO usuarioDAO;

    @Autowired
    public JavaMailSender emailSender;

    public OrganizadoraEventosImp() {
        usuarios = new TreeMap<>();
        eventos = new TreeMap<>();
    }

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // DAO Listo
    public void registrarUsuario(UsuarioDTO usuarioDTO){
        Usuario usuario = usuarioDTO.toEntity();
        usuarioDAO.insertar(usuario);
    }

    public UsuarioDTO obtenerUsuario(String dni){
        // Valida campos vacios
    	Usuario usuario = usuarioDAO.buscar(dni);
    	UsuarioDTO usuarioDTO = null;
    	
    	if (usuario!=null) {
    		usuarioDTO = new UsuarioDTO(usuario);
		}
    	
    	return usuarioDTO;
    }

    // DAO Listo
    public void crearEvento(EventoDTO eventoDTO){
        Evento evento = eventoDTO.toEntity();
        eventoDAO.insertar(evento);
    }
   
    public void inscribirEvento(int id, String dni)
            throws InscripcionInvalidaException{
        Evento evento = null;
        Usuario usuario = null;
        
        evento = eventoDAO.buscar(id);
        usuario = usuarioDAO.buscar(dni);
        
        //CAMBIAR
        // Valida si el usuario no esta ya inscrito en la lista de invitados
        if (!eventoDAO.validarInvitadoLista(evento, usuario)) {
            // Valida que haya cupo para entrar al evento
            if (eventoDAO.obtenerSaturacion(evento) < evento.getCupo()) {
                eventoDAO.inscribirInvitado(evento, usuario);
            } else {
                eventoDAO.inscribirEspera(evento, usuario);
            }
        } else {
            throw new InscripcionInvalidaException();
        }
    }

    // DAO Listo
    public void cancelarInscripcion(int id, String dni)
            throws CancelacionInvalidaException, UsuarioNoRegistradoNoEncontradoException {
    	Evento evento = null;
        Usuario usuarioCancela = null;
        
        evento = eventoDAO.buscar(id);
        usuarioCancela = usuarioDAO.buscar(dni);

        // Valida si el usuario se encuentra en la lista de invitados
        if (eventoDAO.validarInvitadoLista(evento, usuarioCancela)) {
            // Valida que no se haya celebrado aun el evento
            if (eventoDAO.buscar(evento.getId()).compararConFechaActual()) {
                Object[] datosListaEspera = eventoDAO.obtenerSiguienteDeListaEspera(evento);
                
                if(datosListaEspera!=null) {
                	Usuario usuarioEntra = usuarioDAO.buscar(datosListaEspera[1].toString());
                	eventoDAO.sacarDeListaDeEspera(datosListaEspera, evento);
                	eventoDAO.inscribirInvitado(evento, usuarioEntra);
                	SimpleMailMessage mensaje = new SimpleMailMessage();
                    mensaje.setTo(usuarioEntra.getCorreo());
                    mensaje.setSubject("Has sido aceptado en un evento");
                    String textoMensaje = "Enhorabuena! Has sido aceptado para la actividad " + evento.getNombre() + " a celebrar el día \n"
                            + evento.getFecha() + " en " + evento.getLugar() + ". Entra en tu cuenta de Organizadora de Eventos para obtener más \n" + "información. ";
                    mensaje.setText(textoMensaje);
                    emailSender.send(mensaje);
                }
                eventoDAO.cancelarInvitado(evento, usuarioCancela);
                
            } else {
                throw new CancelacionInvalidaException();
            }
        } else {
            throw new UsuarioNoRegistradoNoEncontradoException();
        }
    }

    
    public void cancelarListaEspera(EventoDTO eventoDTO) throws CancelacionInvalidaException {
        Evento evento = eventoDTO.toEntity();
        evento = eventoDAO.buscar(evento.getId());
        
        //CAMBIAR
        Usuario usuario = null;

        eventoDAO.sacarDeListaDeEspera(eventoDAO.obtenerDatosListaEsperaParaCancelar(evento, usuario), evento);
    }
    
    public EventoDTO buscarEventoPorId(int id) {
    	Evento evento = eventoDAO.buscar(id);
    	EventoDTO eventoDTO = null;
    	
    	if (evento!=null) {
    		eventoDTO = new EventoDTO(evento);
		}
    	
    	return eventoDTO;
    }

    // DAO Listo
    public List<EventoDTO> buscarEvento(String attr) {
        List<Evento> eventosBuscados = eventoDAO.buscarEventoPorTipoYDescripcion(attr);
        List<EventoDTO> eventosBuscadosDTO = new ArrayList<>();

        for (Evento evento : eventosBuscados) {
            eventosBuscadosDTO.add(new EventoDTO(evento));
        }
        return eventosBuscadosDTO;
    }

    // DAO Listo
    public void cancelarEvento(int id)
            throws CancelacionInvalidaException {
    	Evento evento = eventoDAO.buscar(id);
    	
        if (evento !=null && eventoDAO.buscar(evento.getId()).compararConFechaActual()) {
        	eventoDAO.borrar(evento);
        } else {
            throw new CancelacionInvalidaException();
        }
    }

    // DAO listo
    public List<EventoDTO> listarEventoInscritoCelebrado() {
        List<EventoDTO> eventosInscritosCelebrados = new ArrayList<EventoDTO>();
        //MODIFICAR
        Usuario usuario = null;
        for (Evento evento : eventoDAO.listarEventoInscrito(usuario)) {
            if (!evento.compararConFechaActual()) {
                eventosInscritosCelebrados.add(new EventoDTO(evento));
            }
        }
        return eventosInscritosCelebrados;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoInscritoPorCelebrar() {
        List<EventoDTO> eventosInscritosPorCelebrar = new ArrayList<EventoDTO>();
        //MODIFICAR
        Usuario usuario = null;
        for (Evento evento : eventoDAO.listarEventoInscrito(usuario)) {
            if (evento.compararConFechaActual()) {
                eventosInscritosPorCelebrar.add(new EventoDTO(evento));
            }
        }
        return eventosInscritosPorCelebrar;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoEsperaPorCelebrar() {
        List<EventoDTO> eventosEsperaPorCelebrar = new ArrayList<EventoDTO>();
        //MODIFICAR
        Usuario usuario = null;
        for (Evento evento : eventoDAO.listarEventoEspera(usuario)) {
            if (evento.compararConFechaActual()) {
                eventosEsperaPorCelebrar.add(new EventoDTO(evento));
            }
        }
        return eventosEsperaPorCelebrar;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoEsperaCelebrado() {
        List<EventoDTO> eventosEsperaCelebrado = new ArrayList<EventoDTO>();
        //MODIFICAR
        Usuario usuario = null;
        for (Evento evento : eventoDAO.listarEventoEspera(usuario)) {
            if (!evento.compararConFechaActual()) {
                eventosEsperaCelebrado.add(new EventoDTO(evento));
            }
        }
        return eventosEsperaCelebrado;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoOrganizadoCelebrado() {
        List<EventoDTO> eventosOrganizadosCelebrados = new ArrayList<EventoDTO>();
        //MODIFICAR
        Usuario usuario = null;
        for (Evento evento : eventoDAO.listarEventoOrganizado(usuario)) {
            if (!evento.compararConFechaActual()) {
                eventosOrganizadosCelebrados.add(new EventoDTO(evento));
            }
        }
        return eventosOrganizadosCelebrados;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoOrganizadoPorCelebrar() {
        List<EventoDTO> eventosOrganizadosPorCelebrar = new ArrayList<EventoDTO>();
        //MODIFICAR
        Usuario usuario = null;
        for (Evento evento : eventoDAO.listarEventoOrganizado(usuario)) {
            if (evento.compararConFechaActual()) {
                eventosOrganizadosPorCelebrar.add(new EventoDTO(evento));
            }
        }
        return eventosOrganizadosPorCelebrar;
    }


}

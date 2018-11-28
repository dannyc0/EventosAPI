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
import es.ujaen.dae.eventosapi.exception.SesionNoIniciadaException;
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
    boolean isLogeado;
    long token;

    Hashtable<Long, String> usuariosTokens;
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
        usuariosTokens = new Hashtable<>();
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
    public void registrarUsuario(UsuarioDTO usuarioDTO, String password) throws CamposVaciosException {
        String mensaje = "";
        Usuario usuario = usuarioDTO.toEntity();

        // Valida campos vacios
        if (usuario.getDni() != null && !usuario.getDni().isEmpty() && password != null && !password.isEmpty()
                && usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
            usuario.setPassword(password);
            usuarioDAO.insertar(usuario);
        } else {
            throw new CamposVaciosException();
        }
    }

    // DAO Listo
    public UsuarioDTO identificarUsuario(String dni, String password)
            throws UsuarioNoRegistradoNoEncontradoException, CamposVaciosException {
        Usuario usuario;
        long respuesta = 0;

        // Valida campos vacios
        if (!dni.isEmpty() && !password.isEmpty()) {
            usuario = usuarioDAO.buscar(dni);
            // Si el usuario existe y la contraseña es correcta
            if (usuario != null && usuario.getPassword().equals(password)) {
                token = generarToken();
                respuesta = token;
                usuariosTokens.put(token, dni);
            } else {
                throw new UsuarioNoRegistradoNoEncontradoException();
            }
        } else {
            throw new CamposVaciosException();
        }
        return new UsuarioDTO(usuario);
    }

    public boolean cerrarSesion(long token) {
        if (usuariosTokens.remove(token) != null) {
            return true;
        }
        return false;
    }

    // DAO Listo
    public void crearEvento(EventoDTO eventoDTO, long token)
            throws CamposVaciosException, SesionNoIniciadaException, FechaInvalidaException {
        Evento evento = eventoDTO.toEntity();
        String mensaje = "";
        // Valida si hay una sesion iniciada
        if (validarToken(token)) {
            evento.setOrganizador(usuarioDAO.buscar(usuariosTokens.get(token)));
            // Valida si hay campos vacios
            if (evento.getNombre() != null && !evento.getNombre().isEmpty() && evento.getDescripcion() != null
                    && !evento.getDescripcion().isEmpty() && evento.getFecha() != null && !evento.getFecha().isEmpty()
                    && evento.getLugar() != null && !evento.getLugar().isEmpty() && evento.getCupo() != 0) {
                eventoDAO.insertar(evento);
            } else {
                throw new CamposVaciosException();
            }
        } else {
            throw new SesionNoIniciadaException();
        }
    }

    // DAO Listo
    public void inscribirEvento(EventoDTO eventoDTO, long token)
            throws InscripcionInvalidaException, SesionNoIniciadaException, FechaInvalidaException {
        // Valida si hay una sesion iniciada
        if (validarToken(token)) {
            Evento evento = eventoDTO.toEntity();
            evento = eventoDAO.buscar(evento.getId());
            Usuario usuario = usuarioDAO.buscar(usuariosTokens.get(token));

            // Valida si el evento aun no se ha celebrado por la fecha
            if (eventoDAO.buscar(evento.getId()).compararConFechaActual()) {
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
            } else {
                throw new FechaInvalidaException();
            }
        } else {
            throw new SesionNoIniciadaException();
        }
    }

    // DAO Listo
    public void cancelarInscripcion(EventoDTO eventoDTO, long token)
            throws CancelacionInvalidaException, SesionNoIniciadaException, UsuarioNoRegistradoNoEncontradoException {
        if (validarToken(token)) {
            Evento evento = eventoDTO.toEntity();
            evento = eventoDAO.buscar(evento.getId());
            Usuario usuarioCancela = usuarioDAO.buscar(usuariosTokens.get(token));

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
        } else {
            throw new SesionNoIniciadaException();
        }
    }

    // DAO Listo
    public void cancelarListaEspera(EventoDTO eventoDTO, long token) throws CancelacionInvalidaException {
        Evento evento = eventoDTO.toEntity();
        evento = eventoDAO.buscar(evento.getId());
        Usuario usuario = usuarioDAO.buscar(usuariosTokens.get(token));

        eventoDAO.sacarDeListaDeEspera(eventoDAO.obtenerDatosListaEsperaParaCancelar(evento, usuario), evento);
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
    public void cancelarEvento(EventoDTO eventoDTO, long token)
            throws CancelacionInvalidaException, SesionNoIniciadaException {
        Evento evento = eventoDTO.toEntity();
        if (validarToken(token)) {
            if (eventoDAO.buscar(evento.getId()).compararConFechaActual()) {
                if (eventoDAO.obtenerOrganizadorEvento(evento.getId()).getDni().equals(usuariosTokens.get(token))) {
                    eventoDAO.borrar(evento);
                } else {
                    throw new CancelacionInvalidaException();
                }
            } else {
                throw new CancelacionInvalidaException();
            }
        } else {
            throw new SesionNoIniciadaException();
        }
    }

    // DAO listo
    public List<EventoDTO> listarEventoInscritoCelebrado(long token) {
        List<EventoDTO> eventosInscritosCelebrados = new ArrayList<EventoDTO>();
        if (validarToken(token)) {
            Usuario usuario = usuarioDAO.buscar(usuariosTokens.get(token));
            for (Evento evento : eventoDAO.listarEventoInscrito(usuario)) {
                if (!evento.compararConFechaActual()) {
                    eventosInscritosCelebrados.add(new EventoDTO(evento));
                }
            }
        }
        return eventosInscritosCelebrados;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoInscritoPorCelebrar(long token) {
        List<EventoDTO> eventosInscritosPorCelebrar = new ArrayList<EventoDTO>();
        if (validarToken(token)) {
            Usuario usuario = usuarioDAO.buscar(usuariosTokens.get(token));
            for (Evento evento : eventoDAO.listarEventoInscrito(usuario)) {
                if (evento.compararConFechaActual()) {
                    eventosInscritosPorCelebrar.add(new EventoDTO(evento));
                }
            }
        }
        return eventosInscritosPorCelebrar;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoEsperaPorCelebrar(long token) {
        List<EventoDTO> eventosEsperaPorCelebrar = new ArrayList<EventoDTO>();
        if (validarToken(token)) {
            Usuario usuario = usuarioDAO.buscar(usuariosTokens.get(token));
            for (Evento evento : eventoDAO.listarEventoEspera(usuario)) {
                if (evento.compararConFechaActual()) {
                    eventosEsperaPorCelebrar.add(new EventoDTO(evento));
                }
            }
        }
        return eventosEsperaPorCelebrar;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoEsperaCelebrado(long token) {
        List<EventoDTO> eventosEsperaCelebrado = new ArrayList<EventoDTO>();
        if (validarToken(token)) {
            Usuario usuario = usuarioDAO.buscar(usuariosTokens.get(token));
            for (Evento evento : eventoDAO.listarEventoEspera(usuario)) {
                if (!evento.compararConFechaActual()) {
                    eventosEsperaCelebrado.add(new EventoDTO(evento));
                }
            }
        }
        return eventosEsperaCelebrado;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoOrganizadoCelebrado(long token) {
        List<EventoDTO> eventosOrganizadosCelebrados = new ArrayList<EventoDTO>();
        if (validarToken(token)) {
            Usuario usuario = usuarioDAO.buscar(usuariosTokens.get(token));
            for (Evento evento : eventoDAO.listarEventoOrganizado(usuario)) {
                if (!evento.compararConFechaActual()) {
                    eventosOrganizadosCelebrados.add(new EventoDTO(evento));
                }
            }
        }
        return eventosOrganizadosCelebrados;
    }

    // DAO Listo
    public List<EventoDTO> listarEventoOrganizadoPorCelebrar(long token) {
        List<EventoDTO> eventosOrganizadosPorCelebrar = new ArrayList<EventoDTO>();
        if (validarToken(token)) {
            Usuario usuario = usuarioDAO.buscar(usuariosTokens.get(token));
            for (Evento evento : eventoDAO.listarEventoOrganizado(usuario)) {
                if (evento.compararConFechaActual()) {
                    eventosOrganizadosPorCelebrar.add(new EventoDTO(evento));
                }
            }
        }
        return eventosOrganizadosPorCelebrar;
    }

    private long generarToken() {
        Long tok;
        Random random = new Random();
        tok = random.nextLong();
        if (tok < 0) {
            return tok * -1;
        }
        return tok;
    }

    private boolean validarToken(long token) {
        if (usuariosTokens.containsKey(token)) {
            return true;
        }
        return false;
    }

}

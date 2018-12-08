package es.ujaen.dae.eventosapi.servicio;

import java.util.List;

import es.ujaen.dae.eventosapi.dto.EventoDTO;
import es.ujaen.dae.eventosapi.dto.UsuarioDTO;
import es.ujaen.dae.eventosapi.exception.CamposVaciosException;
import es.ujaen.dae.eventosapi.exception.CancelacionInvalidaException;
import es.ujaen.dae.eventosapi.exception.FechaInvalidaException;
import es.ujaen.dae.eventosapi.exception.InscripcionInvalidaException;
import es.ujaen.dae.eventosapi.exception.SesionNoIniciadaException;
import es.ujaen.dae.eventosapi.exception.UsuarioNoRegistradoNoEncontradoException;

public interface OrganizadoraEventosService {

    // mostrar usuarios
    // public void obtenerUsuarios();
    // public void obtenerEventos();
    /////////////////////////
    public void registrarUsuario(UsuarioDTO usuarioDTO, String password) throws CamposVaciosException;// Probado

    public void crearEvento(EventoDTO eventoDTO)
            throws CamposVaciosException, SesionNoIniciadaException, FechaInvalidaException;// Probado

    public void inscribirEvento(EventoDTO eventoDTO)
            throws InscripcionInvalidaException, SesionNoIniciadaException, FechaInvalidaException;// Probado

    public void cancelarInscripcion(EventoDTO eventoDTO)
            throws CancelacionInvalidaException, SesionNoIniciadaException, UsuarioNoRegistradoNoEncontradoException;// Probado

    public List<EventoDTO> buscarEvento(String attr);// Probado

    public void cancelarEvento(EventoDTO eventoDTO)
            throws CancelacionInvalidaException, SesionNoIniciadaException;// Probado

    public List<EventoDTO> listarEventoInscritoCelebrado();// Probado

    public List<EventoDTO> listarEventoInscritoPorCelebrar();// Probado

    public List<EventoDTO> listarEventoEsperaPorCelebrar(); // Probado

    public List<EventoDTO> listarEventoEsperaCelebrado(); // Probado

    public List<EventoDTO> listarEventoOrganizadoCelebrado();// Probado

    public List<EventoDTO> listarEventoOrganizadoPorCelebrar();// Probado

    public void cancelarListaEspera(EventoDTO eventoDTO) throws CancelacionInvalidaException;

    /*
	 * Corregido: El metodo validar fecha no funcionaba correctamente, la lógica
	 * estaba mal Cancelar inscripcion no quitaba el evento de la lista contenida en
	 * usuario Cancelar evento no estaba definido correctamente
	 * 
	 * Añadido: No existia cerrar sesion Para cancelar inscripcion, no tiene que
	 * haberse celebrado aun Para cancelar evento, no tiene que haberse celebrado
	 * aun Para inscribirse, no tiene que haberse celebrado aun Para inscribirse, no
	 * tiene que estar inscrito previamente
	 * 
	 * 
	 * Preguntar al profesor: Si el token debe ser solicitado al cliente cada vez
	 * que haga una transaccion o es interno Si el id del evento lo ingresa el
	 * cliente Si solo puede haber una sesion abierta a la vez Si el organizador
	 * puede inscribirse al evento que organizó Si se debe validar por ejemplo DNI,
	 * fecha en formato correcto, etc
	 * 
     */
}

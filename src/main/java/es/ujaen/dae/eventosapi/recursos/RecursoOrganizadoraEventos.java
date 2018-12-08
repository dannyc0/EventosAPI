package es.ujaen.dae.eventosapi.recursos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.ujaen.dae.eventosapi.bean.OrganizadoraEventosImp;
import es.ujaen.dae.eventosapi.dto.EventoDTO;
import es.ujaen.dae.eventosapi.dto.UsuarioDTO;
import es.ujaen.dae.eventosapi.exception.CamposVaciosException;
import es.ujaen.dae.eventosapi.exception.CancelacionInvalidaException;
import es.ujaen.dae.eventosapi.exception.EventoInexistenteException;
import es.ujaen.dae.eventosapi.exception.FechaInvalidaException;
import es.ujaen.dae.eventosapi.exception.InscripcionInvalidaException;
import es.ujaen.dae.eventosapi.exception.UsuarioExistente;
import es.ujaen.dae.eventosapi.exception.UsuarioIncorrecto;
import es.ujaen.dae.eventosapi.exception.UsuarioNoRegistradoNoEncontradoException;
import es.ujaen.dae.eventosapi.modelo.Evento;
import es.ujaen.dae.eventosapi.modelo.Usuario;

@RestController
@RequestMapping("/organizadoraeventos")
public class RecursoOrganizadoraEventos {
	
	@Autowired
	OrganizadoraEventosImp organizadoraEventos;
	
	@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
	@ExceptionHandler({UsuarioIncorrecto.class,CamposVaciosException.class,FechaInvalidaException.class, NumberFormatException.class})
	public void handlerParametroIncorrecto() {}
	
	@ResponseStatus(code = HttpStatus.CONFLICT)
	@ExceptionHandler({UsuarioExistente.class,InscripcionInvalidaException.class,CancelacionInvalidaException.class})
	public void handlerRecursoExistente() {}
	
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	@ExceptionHandler({UsuarioNoRegistradoNoEncontradoException.class, EventoInexistenteException.class})
	public void handlerRecursoNoExistente() {}
	
	//Registrar usuario
	@RequestMapping(value="/usuario/{dni}",method=RequestMethod.POST,produces="application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void registrarUsuario(@PathVariable String dni, @RequestBody UsuarioDTO usuarioDTO) throws UsuarioIncorrecto, UsuarioExistente, CamposVaciosException{
		if (usuarioDTO == null) {
			throw new UsuarioIncorrecto();
		}
		
		if (organizadoraEventos.obtenerUsuario(dni) != null) {
			throw new UsuarioExistente();
		}
		
		// Valida campos vacios
        if (usuarioDTO.getDni() != null && !usuarioDTO.getDni().isEmpty() && usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()
                && usuarioDTO.getNombre() != null && !usuarioDTO.getNombre().isEmpty()) {
        	organizadoraEventos.registrarUsuario(usuarioDTO);
        } else {
            throw new CamposVaciosException();
        }
	}
	
	//Crear evento
	@RequestMapping(value="/usuario/{dni}",method=RequestMethod.GET,produces="application/json")
	public UsuarioDTO obtenerUsuario(@PathVariable String dni) throws UsuarioNoRegistradoNoEncontradoException{
		UsuarioDTO usuario = organizadoraEventos.obtenerUsuario(dni);
		if (usuario==null) {
			throw new UsuarioNoRegistradoNoEncontradoException();
		}
		return usuario;
	}

	//Buscar evento
	@RequestMapping(value="/eventos/{attr}",method=RequestMethod.GET,produces="application/json")
	public List<EventoDTO> obtenerEvento(@PathVariable String attr) throws EventoInexistenteException{
		String usuario;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		List<EventoDTO> eventos = organizadoraEventos.buscarEvento(attr);
		
		if (eventos.isEmpty()) {
			throw new EventoInexistenteException();
		}
		
		return eventos;
	}
	
	//Crear evento
	@RequestMapping(value="/eventos",method=RequestMethod.POST,produces="application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void crearEvento(@RequestBody EventoDTO eventoDTO) throws FechaInvalidaException{
		String usuario;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		UsuarioDTO organizador = organizadoraEventos.obtenerUsuario(usuario);
		eventoDTO.setOrganizador(organizador);
		
		organizadoraEventos.crearEvento(eventoDTO);
	}
	
	//Inscribir evento
	@RequestMapping(value="/eventos/{id}",method=RequestMethod.PUT,produces="application/json")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void inscribirEvento(@PathVariable String id) throws NumberFormatException, InscripcionInvalidaException{
		String usuario;
		int id_numero;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		
		try {
			id_numero = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			throw new NumberFormatException();
		}
		
		try {
			organizadoraEventos.inscribirEvento(id_numero, usuario);
		} catch (InscripcionInvalidaException e) {
			throw new InscripcionInvalidaException();
		}
	}
	
	//Cancelar inscripcion
	@RequestMapping(value="usuario/{dni}/eventosinscrito/{id}",method=RequestMethod.PUT,produces="application/json")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void cancelarInscripcionEvento(@PathVariable String dni,@PathVariable String id) throws NumberFormatException, EventoInexistenteException, CancelacionInvalidaException{
		int id_numero;
		
		try {
			id_numero = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			throw new NumberFormatException();
		}
		
		if (organizadoraEventos.buscarEventoPorId(id_numero) == null) {
			throw new EventoInexistenteException();
		}
		
		try {
			organizadoraEventos.cancelarInscripcion(id_numero,dni);
		} catch (CancelacionInvalidaException e) {
			throw new CancelacionInvalidaException();
		}
	}
	
	//Cancelar EVENTO
	@RequestMapping(value="usuario/{dni}/eventosorganizados/{id}",method=RequestMethod.DELETE,produces="application/json")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void cancelarEvento(@PathVariable String dni,@PathVariable String id) throws NumberFormatException, EventoInexistenteException, CancelacionInvalidaException{
		int id_numero;
		
		try {
			id_numero = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			throw new NumberFormatException();
		}
		
		if (organizadoraEventos.buscarEventoPorId(id_numero) == null) {
			throw new EventoInexistenteException();
		}
		
		try {
			organizadoraEventos.cancelarEvento(id_numero);
		} catch (CancelacionInvalidaException e) {
			throw new CancelacionInvalidaException();
		}
	}
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public OrganizadoraEventosImp obtenerOrganizadora() {
		return organizadoraEventos;
	}
}

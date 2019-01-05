package es.ujaen.dae.eventosapi.recursos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.fabric.xmlrpc.base.Array;

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
import javassist.expr.NewArray;

@CrossOrigin
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
		
		try {
			organizadoraEventos.registrarUsuario(usuarioDTO);
		} catch (Exception e) {
			throw new CamposVaciosException();
		}
	}
	
	//Obtener usuario por DNI
	@RequestMapping(value="/usuario/{dni}",method=RequestMethod.GET,produces="application/json")
	public UsuarioDTO obtenerUsuario(@PathVariable String dni) throws UsuarioNoRegistradoNoEncontradoException, CamposVaciosException{
		String usuario;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		UsuarioDTO usuarioDTO = null;
		try {
			usuarioDTO = organizadoraEventos.obtenerUsuario(dni);
		} catch (Exception e) {
			throw new CamposVaciosException();
		}
		
		if (usuarioDTO==null) {
			throw new UsuarioNoRegistradoNoEncontradoException();
		}if(usuario.equals(usuarioDTO.getDni())) {
			return usuarioDTO;
		}else {
			throw new UsuarioNoRegistradoNoEncontradoException();
		}
	}

	//Buscar evento por tipo y descripcion
	@RequestMapping(value="/eventos",method=RequestMethod.GET,produces="application/json")
	public List<EventoDTO> obtenerEvento(@RequestParam("tipo") Optional<String> tipo, 
			@RequestParam("descripcion") Optional<String> desc) 
					throws EventoInexistenteException{
		List<EventoDTO> eventos = new ArrayList<EventoDTO>();
		if(tipo.isPresent()) {
			eventos = organizadoraEventos.buscarEventoPorTipo(tipo.get());
		}
		
		if(desc.isPresent()) {
			eventos = organizadoraEventos.buscarEventoPorDescripcion(desc.get());
		}
		
		if (eventos.isEmpty()) {
			throw new EventoInexistenteException();
		}
		return eventos;
	}
	
	//Buscar evento por ID
	@RequestMapping(value="/evento/{id}",method=RequestMethod.GET,produces="application/json")
	public EventoDTO obtenerEventoPorId(@PathVariable String id) throws EventoInexistenteException, NumberFormatException{
		int id_numero;
		try {
			id_numero = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			throw new NumberFormatException();
		}
		
		EventoDTO evento = organizadoraEventos.buscarEventoPorId(id_numero);
		
		if (evento==null) {
			throw new EventoInexistenteException();
		}
		
		return evento;
	}
	
	//Crear evento
	@RequestMapping(value="/eventos",method=RequestMethod.POST,produces="application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void crearEvento(@RequestBody EventoDTO eventoDTO) throws FechaInvalidaException, UsuarioNoRegistradoNoEncontradoException, CamposVaciosException{
		String usuario;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		try {
			UsuarioDTO organizador = organizadoraEventos.obtenerUsuario(usuario);
			eventoDTO.setOrganizador(organizador);
			organizadoraEventos.crearEvento(eventoDTO);
		} catch (Exception e) {
			throw new CamposVaciosException();
		}
		
		
	}
	
	//Inscribir evento
	@RequestMapping(value="/eventos/{id}",method=RequestMethod.PUT,produces="application/json")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void inscribirEvento(@PathVariable String id) throws NumberFormatException, InscripcionInvalidaException, FechaInvalidaException{
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
		} catch (FechaInvalidaException e) {
			throw new FechaInvalidaException();
		}
	}
	
	//Cancelar inscripcion
	@RequestMapping(value="usuario/eventosinscrito/{id}",method=RequestMethod.DELETE,produces="application/json")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void cancelarInscripcionEvento(@PathVariable String id) throws NumberFormatException, EventoInexistenteException, CancelacionInvalidaException{
		
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
		
		if (organizadoraEventos.buscarEventoPorId(id_numero) == null) {
			throw new EventoInexistenteException();
		}
		
		try {
			organizadoraEventos.cancelarInscripcion(id_numero,usuario);
		} catch (CancelacionInvalidaException e) {
			throw new CancelacionInvalidaException();
		}
	}
	
	//Cancelar EVENTO
	@RequestMapping(value="usuario/eventosorganizados/{id}",method=RequestMethod.DELETE,produces="application/json")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void cancelarEvento(@PathVariable String id) throws NumberFormatException, EventoInexistenteException, CancelacionInvalidaException{
		int id_numero;
		String usuario;
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
		
		if (organizadoraEventos.buscarEventoPorId(id_numero) == null) {
			throw new EventoInexistenteException();
		}
		
		try {
			organizadoraEventos.cancelarEvento(id_numero, usuario);
		} catch (CancelacionInvalidaException e) {
			throw new CancelacionInvalidaException();
		}
	}
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public OrganizadoraEventosImp obtenerOrganizadora() {
		return organizadoraEventos;
	}
	
	@RequestMapping(value="usuario/eventosinscrito",method=RequestMethod.GET,produces="application/json")
	public List<Link> listarEventosInscrito(@RequestParam(value="celebrado", required = false, defaultValue = "false") boolean celebrado) throws EventoInexistenteException{
		String usuario;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		List<EventoDTO> lista = null;
		List<Link> listaLinks=  new ArrayList<Link>();
		if(celebrado) {
			lista= organizadoraEventos.listarEventoInscritoCelebrado(usuario);			
		}else {
			lista= organizadoraEventos.listarEventoInscritoPorCelebrar(usuario);
		}
		
		for (EventoDTO evento: lista) {
			listaLinks.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(
					RecursoOrganizadoraEventos.class).obtenerEventoPorId(evento.getId()+"")).withSelfRel());
		}
		return listaLinks;
	
	}
	@RequestMapping(value="usuario/eventosespera",method=RequestMethod.GET,produces="application/json")
	public List<EventoDTO> listarEventosEspera(@RequestParam(value="celebrado", required = false, defaultValue = "false") boolean celebrado) throws EventoInexistenteException{
		String usuario;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		if(celebrado) {
			return organizadoraEventos.listarEventoEsperaCelebrado(usuario);			
		}else {
			return organizadoraEventos.listarEventoEsperaPorCelebrar(usuario);
		}
	}
	
	@RequestMapping(value="usuario/eventosorganizados",method=RequestMethod.GET,produces="application/json")
	public List<EventoDTO> listarEventosOrganizados(@RequestParam(value="celebrado", required = false, defaultValue = "false") boolean celebrado) throws EventoInexistenteException{
		String usuario;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		if(celebrado) {
			return organizadoraEventos.listarEventoOrganizadoCelebrado(usuario);			
		}else {
			return organizadoraEventos.listarEventoOrganizadoPorCelebrar(usuario);
		}
	
	}
}

package es.ujaen.dae.eventosapi.recursos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.ujaen.dae.eventosapi.bean.OrganizadoraEventosImp;
import es.ujaen.dae.eventosapi.dto.EventoDTO;
import es.ujaen.dae.eventosapi.dto.UsuarioDTO;
import es.ujaen.dae.eventosapi.exception.CamposVaciosException;
import es.ujaen.dae.eventosapi.exception.UsuarioNoRegistradoNoEncontradoException;
import es.ujaen.dae.eventosapi.modelo.Evento;
import es.ujaen.dae.eventosapi.modelo.Usuario;

@RestController
@RequestMapping("/organizadoraeventos")
public class RecursoOrganizadoraEventos {
	
	@Autowired
	OrganizadoraEventosImp organizadoraEventos;

	//Buscar evento
	@RequestMapping(value="/eventos/{attr}",method=RequestMethod.GET,produces="application/json")
	public List<EventoDTO> obtenerEvento(@PathVariable String attr) {
		String usuario;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof AnonymousAuthenticationToken) {
			usuario = "anonimo";
		}else {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			usuario = userDetails.getUsername();
		}
		System.out.println(usuario);
		
		List<EventoDTO> eventos = organizadoraEventos.buscarEvento(attr);
		return eventos;
	}
	
	//Crear evento
//	@RequestMapping(value="/eventos/{attr}",method=RequestMethod.GET,produces="application/json")
//	public List<EventoDTO> crearEvento(@PathVariable String attr) {
//		List<EventoDTO> eventos = organizadoraEventos.buscarEvento(attr);
//		return eventos;
//	}
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public OrganizadoraEventosImp obtenerOrganizadora() {
		return organizadoraEventos;
	}
}

package es.ujaen.dae.eventosapi.recursos;

import java.util.List;

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
	
	@RequestMapping(value="/foo", method=RequestMethod.GET,produces="application/json")
	public String[] foo() {
	    String[] arr = { "foo", "foo2" };
	    return arr;
	}

	OrganizadoraEventosImp organizadoraEventos;
	@RequestMapping(value="/eventos/{attr}",method=RequestMethod.GET,produces="application/json")
	public List<EventoDTO> obtenerEvento(@PathVariable String attr) {
		List<EventoDTO> eventos = organizadoraEventos.buscarEvento(attr);
		return eventos;
	}
	
	@RequestMapping(value="/usuario/{dni}",method=RequestMethod.GET,produces="application/json")
	public UsuarioDTO obtenerUsuario(@PathVariable String dni) {
		UsuarioDTO usuario=null;
		try {
			usuario = organizadoraEventos.identificarUsuario(dni, "aaaa");
		} catch (UsuarioNoRegistradoNoEncontradoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CamposVaciosException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return usuario;
	}
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public OrganizadoraEventosImp obtenerOrganizadora() {
		return organizadoraEventos;
	}
}

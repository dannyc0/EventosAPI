package es.ujaen.dae.eventosapi.recursos;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.ujaen.dae.eventosapi.bean.OrganizadoraEventosImp;
import es.ujaen.dae.eventosapi.dto.EventoDTO;
import es.ujaen.dae.eventosapi.modelo.Evento;
import es.ujaen.dae.eventosapi.modelo.Usuario;

@RestController
@RequestMapping("/organizadoraeventos")
public class RecursoOrganizadoraEventos {
	OrganizadoraEventosImp organizadoraEventos;
	@RequestMapping(value="/usuarios/{dni}",method=RequestMethod.GET,produces="application/json")
	public List<EventoDTO> obtenerEvento(@PathVariable String attr) {
		List<EventoDTO> eventos = organizadoraEventos.buscarEvento(attr);
		return eventos;
	}
}

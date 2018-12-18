package es.ujaen.dae.eventosapi.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import es.ujaen.dae.eventosapi.exception.CamposVaciosException;
import es.ujaen.dae.eventosapi.exception.UsuarioNoRegistradoNoEncontradoException;
import es.ujaen.dae.eventosapi.modelo.Usuario;

@Component
public class ServicioDatosUsuarioOrganizadoraEventos implements UserDetailsService{
	@Autowired
	OrganizadoraEventosImp organizadoraEventos;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Usuario usuario = null;
		try {
			usuario = organizadoraEventos.obtenerUsuario(username).toEntity();
		} catch (UsuarioNoRegistradoNoEncontradoException e) {} catch (CamposVaciosException e) {}
		
		if(usuario==null) {
			throw new UsernameNotFoundException(username);
		}
		return User.withUsername(username).password(usuario.getPassword()).roles("USUARIO").build();
	}
}

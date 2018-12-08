package es.ujaen.dae.eventosapi.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SeguridadOrganizadoraEventos extends WebSecurityConfigurerAdapter{
	@Autowired
	ServicioDatosUsuarioOrganizadoraEventos servicioDatosUsuarioOrganizadoraEventos;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(servicioDatosUsuarioOrganizadoraEventos).passwordEncoder(new BCryptPasswordEncoder());
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.httpBasic();
		http.authorizeRequests().antMatchers("/rest/organizadoraeventos/**").hasRole("USUARIO");
	}
}

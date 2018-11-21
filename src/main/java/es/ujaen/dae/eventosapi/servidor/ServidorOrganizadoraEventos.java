package es.ujaen.dae.eventosapi.servidor;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.cache.annotation.EnableCaching;

@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableCaching
@ComponentScan({"es.ujaen.dae.eventosconsolamail.bean", "es.ujaen.dae.eventosconsolamail.dao"})
@EntityScan(basePackages = {"es.ujaen.dae.eventosconsolamail.modelo"})
@SpringBootApplication
public class ServidorOrganizadoraEventos {

    public static void main(String[] args) throws IOException {
        SpringApplication servidor = new SpringApplication(ServidorOrganizadoraEventos.class);
        ApplicationContext ctx = servidor.run(args);
    }
}

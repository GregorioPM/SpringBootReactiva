package com.curso.springboot.reactor.app;

import com.curso.springboot.reactor.app.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Flux<Usuario> nombres = Flux.just("Gregorio" ,"Jose","aa" ,"Perez")
				.map(nombre -> new Usuario(nombre.toUpperCase(),null))
				.doOnNext(usuario -> {
					if(usuario == null){
						throw new RuntimeException("Usuario no puede ser vacios");
					}
					System.out.println(usuario.getNombre());
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		nombres.subscribe(e -> logger.info(e.toString()),
				error -> logger.error(error.getMessage()),
				() -> logger.info("Ha finalizado la ejecución del observable"));
	}
}

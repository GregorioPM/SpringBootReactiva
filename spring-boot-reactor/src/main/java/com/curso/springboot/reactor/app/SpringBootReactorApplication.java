package com.curso.springboot.reactor.app;

import com.curso.springboot.reactor.app.model.Comentarios;
import com.curso.springboot.reactor.app.model.Usuario;
import com.curso.springboot.reactor.app.model.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		//ejemploIterable();
		//ejemploFlatMap();
		//convertirToString();
		//convertirToCollectList();
		//ejemploUsuarioComentariosFlatMap();
		//ejemploUsuarioZipWith();
		//ejemploUsuarioZipWithV2();
		//ejemploRangeZipWith();
		//ejemploInterval();
		//ejemploDelayElements();
		//ejemploIntervalInfinito();
		//ejemploDesdeCreate();
		ejemploContraPresion();
	}

	public void ejemploContraPresion(){
		Flux.range(0,10)
				.log()
				.limitRate(6)
				.subscribe();
				/*.subscribe(new Subscriber<Integer>() {

					Subscription s;

					private Integer limite = 5;
					private Integer consumido = 0;
					@Override
					public void onSubscribe(Subscription s) {
						this.s = s;
						s.request(limite);
					}

					@Override
					public void onNext(Integer t) {
						logger.info(t.toString());
						consumido++;
						if(consumido == limite){
							consumido = 0;
							s.request(limite);
						}
					}

					@Override
					public void onError(Throwable t) {

					}

					@Override
					public void onComplete() {

					}
				});*/
	}

	public void ejemploDesdeCreate() throws InterruptedException {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				Integer contador = 0;
				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}
					if (contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, contador es igual a 5"));
					}
				}
			}, 1000, 1000);
		}).subscribe(numero -> logger.info(numero.toString()),
						error -> logger.error(error.getMessage()),
						()-> logger.info("Hemos terminado"));

	}

	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		Flux.interval(Duration.ofSeconds(1L))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if (i == 5) {
						return Flux.error(new InterruptedException("Solo llega hasta 5"));
					}
					return Flux.just(i);
				})
				.retry(2)
				.subscribe(interval -> logger.info(interval.toString()), error -> logger.error(error.getMessage()));

		latch.await();

	}

	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1,12)
				.delayElements(Duration.ofSeconds(1L))
				.doOnNext(numero -> logger.info(numero.toString()));

		rango.blockLast();
		/*rango.subscribe();

		Thread.sleep(13000);*/

	}

	public void ejemploInterval(){
		Flux<Integer> rango = Flux.range(1,12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1L));

		rango.zipWith(retraso, (ran, retra) -> ran)
				.doOnNext(numero -> logger.info(numero.toString()))
				.blockLast();
	}
	public void ejemploRangeZipWith(){
		Flux.just(1, 2, 3, 4)
				.map(numero -> numero * 2)
				.zipWith(Flux.range(1,4),(uno, dos) -> String.format("Primer flux: %d, Segundo Flux: %d",uno, dos))
				.subscribe(logger::info);
	}
	public void ejemploUsuarioZipWithV2(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon","Doe"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola que tal");
			comentarios.addComentarios("Sirve el comentario");
			comentarios.addComentarios("Prueba final");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u,c);
				});
		usuarioComentariosMono.subscribe(uc -> logger.info(uc.toString()));
	}

	public void ejemploUsuarioZipWith(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon","Doe"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola que tal");
			comentarios.addComentarios("Sirve el comentario");
			comentarios.addComentarios("Prueba final");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario));
		usuarioComentariosMono.subscribe(uc -> logger.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon","Doe"));
		Mono<Comentarios> comentariosUsuario = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola que tal");
			comentarios.addComentarios("Sirve el comentario");
			comentarios.addComentarios("Prueba final");
			return comentarios;
		});

		usuarioMono.flatMap(u -> comentariosUsuario.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> logger.info(uc.toString()));
	}

	public void convertirToCollectList() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();

		usuariosList.add(new Usuario("Gregorio","Perez"));
		usuariosList.add(new Usuario("Jose", "Torres"));
		usuariosList.add(new Usuario("Manosalva", "Soriano"));
		usuariosList.add(new Usuario("Luis", "Perez"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(item -> logger.info(item.toString()));
				});
	}

	public void convertirToString() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();

		usuariosList.add(new Usuario("Gregorio","Perez"));
		usuariosList.add(new Usuario("Jose", "Torres"));
		usuariosList.add(new Usuario("Manosalva", "Soriano"));
		usuariosList.add(new Usuario("Luis", "Perez"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido()))
				.flatMap(nombre -> {
					if(nombre.contains("bruce".toUpperCase())){
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> {
					return nombre.toLowerCase();
				})
				.subscribe(u -> logger.info(u.toString()));
	}

	public void ejemploIterable() throws Exception {

		List<String> usuariosList = new ArrayList<>();

		usuariosList.add("Gregorio Perez");
		usuariosList.add("Jose Torres");
		usuariosList.add("Manosalva Soriano");
		usuariosList.add("Luis Perez");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux<String> nombres = Flux.fromIterable(usuariosList);
		// Flux.just("Gregorio Perez" ,"Jose Torres","Manosalva Soriano" ,"Luis Perez" ,"Bruce Lee", "Bruce Willis" );
		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
				.doOnNext(usuario -> {
					if(usuario == null){
						throw new RuntimeException("Usuario no puede ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(e -> logger.info(e.toString()),
				error -> logger.error(error.getMessage()),
				() -> logger.info("Ha finalizado la ejecución del observable"));
	}

	public void ejemploFlatMap() throws Exception {

		List<String> usuariosList = new ArrayList<>();

		usuariosList.add("Gregorio Perez");
		usuariosList.add("Jose Torres");
		usuariosList.add("Manosalva Soriano");
		usuariosList.add("Luis Perez");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if(usuario.getNombre().equalsIgnoreCase("bruce")){
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
				.subscribe(u -> logger.info(u.toString()));
	}
}

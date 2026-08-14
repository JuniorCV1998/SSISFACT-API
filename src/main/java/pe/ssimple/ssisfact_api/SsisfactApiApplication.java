package pe.ssimple.ssisfact_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SsisfactApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsisfactApiApplication.class, args);
        System.out.println("Aplicación iniciada correctamente :D");
	}

}

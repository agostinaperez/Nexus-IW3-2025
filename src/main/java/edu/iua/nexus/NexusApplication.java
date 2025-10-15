package edu.iua.nexus;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
/*la app está preparada desde el initializer para ser empaquetada como jar, pero si quiero q pueda correr
tamb en un application server (war) debo extender la clase SpringBootServletInitializer e implementar el método
tamb quiero q cuando bootee me muestre el perfil activo
EL commandLInerunner hace que se ejecute el método run al iniciar la app y q esté todo bien
*/
public class NexusApplication extends SpringBootServletInitializer implements CommandLineRunner {

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		SpringApplication.run(NexusApplication.class, args);
	}

	@Value("${spring.profiles.active}")
	private String profile;

	@Override
	public void run(String... args) throws Exception {
		String[] profiles = env.getActiveProfiles();
		if (profiles.length == 0) {
			log.info("No hay perfiles activos (usando configuración por defecto)");
		} else {
			log.info("Perfiles activos: {}", String.join(", ", profiles));
		}	}
}
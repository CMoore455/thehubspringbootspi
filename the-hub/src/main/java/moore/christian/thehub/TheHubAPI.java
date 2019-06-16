package moore.christian.thehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.SpringServletContainerInitializer;

@SpringBootApplication
@ComponentScan
public class TheHubAPI extends SpringServletContainerInitializer {

	public static void main(String[] args) {
		SpringApplication.run(TheHubAPI.class, args);
	}

}
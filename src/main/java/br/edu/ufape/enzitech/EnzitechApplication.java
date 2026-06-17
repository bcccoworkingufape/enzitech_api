package br.edu.ufape.enzitech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class EnzitechApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnzitechApplication.class, args);
	}

}

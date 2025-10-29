
/*
 * GoHostApplication — Punto de entrada de Spring Boot
 * @SpringBootApplication habilita autoconfiguración y escaneo de componentes.
 * Al ejecutar main(), arranca Tomcat embebido y expone los controladores REST.
 */
package co.edu.uniquindio.gohost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//anotación principal que combina @Configuration, @EnableAutoConfiguration y @ComponentScan
@SpringBootApplication
@EnableScheduling
public class GoHostApplication {

    //método que arranca toda la aplicación y el servidor web
    public static void main(String[] args) {
        SpringApplication.run(GoHostApplication.class, args);
    }
}

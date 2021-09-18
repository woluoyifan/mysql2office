package com.luoyifan.mysql2office;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(App.class, args);
        String port = ctx.getEnvironment().getProperty("local.server.port");
        System.out.println("Please access http://localhost:" + port + " using a modern browser (Chrome, Firefox, Edge ...)");
        System.out.println("Please access http://localhost:" + port + " using a modern browser (Chrome, Firefox, Edge ...)");
        System.out.println("Please access http://localhost:" + port + " using a modern browser (Chrome, Firefox, Edge ...)");
    }
}

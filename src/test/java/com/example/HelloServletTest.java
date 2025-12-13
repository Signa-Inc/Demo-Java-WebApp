package com.example;

import io.restassured.RestAssured;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class HelloServletTest {

    private static Tomcat tomcat;
    private static int port = 0;

    @BeforeAll
    public static void startTomcat() throws LifecycleException {
        tomcat = new Tomcat();
        // Случайный порт
        port = Integer.parseInt(System.getProperty("tomcat.test.port", "0"));
        tomcat.setPort(port);
        tomcat.getConnector(); // инициализация

        // Создаём контекст приложения
        Context ctx = tomcat.addContext("/webapp-demo", null);
        Tomcat.addServlet(ctx, "helloServlet", new HelloServlet());
        ctx.addServletMappingDecoded("/hello", "helloServlet");

        tomcat.start();

        // Устанавливаем порт для RestAssured
        RestAssured.port = tomcat.getConnector().getLocalPort();
        RestAssured.basePath = "/webapp-demo";
    }

    @AfterAll
    public static void stopTomcat() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Test
    public void shouldReturnHelloMessage() {
        given()
            .when()
                .get("/hello")
            .then()
                .statusCode(200)
                .body(containsString("Hello from CI/CD Pipeline!"));
    }
}
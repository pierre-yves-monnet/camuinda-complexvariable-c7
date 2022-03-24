package org.camunda.complexvariable.c7.engine;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// this tag is mandatory to accept forms
@EnableProcessApplication

public class CamundaEngine {
    public static void main(String... args) {
        SpringApplication.run(CamundaEngine.class, args);
    }
}



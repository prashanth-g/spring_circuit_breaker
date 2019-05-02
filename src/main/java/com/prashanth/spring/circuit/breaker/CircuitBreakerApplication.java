package com.prashanth.spring.circuit.breaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@SpringBootApplication
public class CircuitBreakerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircuitBreakerApplication.class, args);
    }
}

@Service
class FailingService {

    Mono<String> greet(Optional<String> name) {
        return name.map(str -> Mono.just("Hola " + str))
                    .orElse(Mono.error(new NullPointerException()));
    }
}

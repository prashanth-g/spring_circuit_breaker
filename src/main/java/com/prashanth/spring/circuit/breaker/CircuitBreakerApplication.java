package com.prashanth.spring.circuit.breaker;

import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@SpringBootApplication
public class CircuitBreakerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircuitBreakerApplication.class, args);
    }
}

@RestController
class FailingRestController {
    private final FailingService failingService;

    FailingRestController(FailingService failingService) {
        this.failingService = failingService;
    }

    @GetMapping("/greet")
    Publisher<String> greet(@RequestParam Optional<String> name) {
        return this.failingService.greet(name);
    }
}

@Service
class FailingService {

    Mono<String> greet(Optional<String> name) {
        return name.map(str -> Mono.just("Hola " + str))
                    .orElse(Mono.error(new NullPointerException()));
    }
}


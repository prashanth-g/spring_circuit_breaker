package com.prashanth.spring.circuit.breaker;

import org.reactivestreams.Publisher;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

@RestController
public class FailingRestController {
    private final FailingService failingService;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;

    FailingRestController(FailingService failingService,
                          ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory) {
        this.failingService = failingService;
        this.reactiveCircuitBreaker = reactiveCircuitBreakerFactory.create("greet");
    }

    @GetMapping("/greet")
    Publisher<String> greet(@RequestParam Optional<String> name) {
        Mono<String> greetMessage = this.failingService.greet(name);
        return this.reactiveCircuitBreaker.run(greetMessage, new Function<Throwable, Mono<String>>() {
            @Override
            public Mono<String> apply(Throwable throwable) {
                return Mono.just("Hola from failing service!");
            }
        });
    }
}
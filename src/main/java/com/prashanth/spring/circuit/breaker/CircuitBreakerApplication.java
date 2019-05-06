package com.prashanth.spring.circuit.breaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

@SpringBootApplication
public class CircuitBreakerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircuitBreakerApplication.class, args);
    }

    @Bean
    ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory() {
        ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory();
        factory.configureDefault(new Function<String, Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration>() {
            @Override
            public Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration apply(String s) {
                return new Resilience4JConfigBuilder(s)
                        .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(3)).build())
                        .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                        .build();
            }
        });

        return factory;
    }
}

@RestController
class FailingRestController {
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

@Log4j2
@Service
class FailingService {

    Mono<String> greet(Optional<String> name) {
        long secondsToDelay = (long) (Math.random() * 10);
        return name.map(str -> {
                        String message = "Hola " + str + " in " + secondsToDelay;
                        log.info(message);
                        return Mono.just(message);
                     })
                    .orElse(Mono.error(new NullPointerException()))
                    .delayElement(Duration.ofSeconds(secondsToDelay));
    }
}


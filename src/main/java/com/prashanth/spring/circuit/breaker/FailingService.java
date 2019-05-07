package com.prashanth.spring.circuit.breaker;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Log4j2
@Service
public class FailingService {

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
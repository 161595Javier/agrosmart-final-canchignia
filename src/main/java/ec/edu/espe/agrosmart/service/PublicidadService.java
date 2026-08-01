package ec.edu.espe.agrosmart.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class PublicidadService {

    private final AgroSmartAIService aiService;

    public PublicidadService(AgroSmartAIService aiService) {
        this.aiService = aiService;
    }

    public Mono<String> generarPublicidad(
            String producto,
            String audiencia) {

        return Mono.fromCallable(
                        () -> aiService.generarPublicidad(producto, audiencia)
                )
                // La llamada HTTP al modelo es bloqueante.
                .subscribeOn(Schedulers.boundedElastic())

                // Evita esperar indefinidamente al proveedor.
                .timeout(Duration.ofSeconds(30))

                // Un fallo externo no debe derribar el endpoint.
                .onErrorResume(error -> Mono.just(
                        "Publicidad no disponible en este momento ("
                                + error.getClass().getSimpleName()
                                + ")"
                ));
    }
}
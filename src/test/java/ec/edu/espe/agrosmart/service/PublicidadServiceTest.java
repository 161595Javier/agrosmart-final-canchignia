package ec.edu.espe.agrosmart.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PublicidadServiceTest {

    @Test
    void generarPublicidad_cuandoElProveedorResponde_debeEmitirElTexto() {
        // Arrange
        AgroSmartAIService aiService =
                Mockito.mock(AgroSmartAIService.class);

        Mockito.when(
                aiService.generarPublicidad(
                        "Café",
                        "cafeterías de especialidad"
                )
        ).thenReturn(
                "Café de altura para experiencias excepcionales"
        );

        PublicidadService service =
                new PublicidadService(aiService);

        // Act
        Mono<String> resultado =
                service.generarPublicidad(
                        "Café",
                        "cafeterías de especialidad"
                );

        // Assert
        StepVerifier.create(resultado)
                .expectNext(
                        "Café de altura para experiencias excepcionales"
                )
                .verifyComplete();

        Mockito.verify(aiService)
                .generarPublicidad(
                        "Café",
                        "cafeterías de especialidad"
                );
    }

    @Test
    void generarPublicidad_cuandoElProveedorFalla_debeEmitirMensajeDeRespaldo() {
        // Arrange
        AgroSmartAIService aiService =
                Mockito.mock(AgroSmartAIService.class);

        Mockito.when(
                aiService.generarPublicidad(
                        Mockito.anyString(),
                        Mockito.anyString()
                )
        ).thenThrow(
                new RuntimeException("429 Too Many Requests")
        );

        PublicidadService service =
                new PublicidadService(aiService);

        // Act
        Mono<String> resultado =
                service.generarPublicidad(
                        "Café",
                        "cafeterías"
                );

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(texto ->
                        texto.contains(
                                "Publicidad no disponible"
                        )
                                && texto.contains(
                                "RuntimeException"
                        )
                )
                .verifyComplete();
    }
}
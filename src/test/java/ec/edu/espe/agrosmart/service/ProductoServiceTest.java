package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class ProductoServiceTest {

    @Test
    void obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirTres() {
        // Arrange
        ProductoRepository repository =
                Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findAll())
                .thenReturn(datosConTresValidosYDosInvalidos());

        ProductoService service =
                new ProductoService(repository);

        // Act
        Flux<Producto> flujo =
                service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextCount(3)
                .verifyComplete();

        Mockito.verify(repository).findAll();
    }

    @Test
    void obtenerProductosComercializables_conTodosInvalidos_debeEmitirProductoGenerico() {
        // Arrange
        ProductoRepository repository =
                Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findAll())
                .thenReturn(List.of(
                        crearProducto(
                                "Café sin precio",
                                "0.00",
                                "ventas@agrosmart.ec"
                        ),
                        crearProducto(
                                "Café sin correo",
                                "10.00",
                                ""
                        )
                ));

        ProductoService service =
                new ProductoService(repository);

        // Act
        Flux<Producto> flujo =
                service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextMatches(producto ->
                        producto.getId().equals(0L)
                                && producto.getNombre()
                                .equals("PRODUCTO GENÉRICO")
                )
                .verifyComplete();
    }

    @Test
    void buscarPorId_conIdExistente_debeEmitirProducto() {
        // Arrange
        ProductoRepository repository =
                Mockito.mock(ProductoRepository.class);

        ProductoEntity entity = crearProducto(
                "Café arábigo de altura",
                "18.50",
                "ventas@agrosmart.ec"
        );

        Mockito.when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        ProductoService service =
                new ProductoService(repository);

        // Act
        Mono<Producto> resultado =
                service.buscarPorId(1L);

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(producto ->
                        producto.getNombre()
                                .equals("Café arábigo de altura")
                                && producto.getPrecioUsd()
                                .compareTo(
                                        new BigDecimal("18.50")
                                ) == 0
                )
                .verifyComplete();
    }

    @Test
    void buscarPorId_conIdInexistente_debeEmitirError() {
        // Arrange
        ProductoRepository repository =
                Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findById(9999L))
                .thenReturn(Optional.empty());

        ProductoService service =
                new ProductoService(repository);

        // Act
        Mono<Producto> resultado =
                service.buscarPorId(9999L);

        // Assert
        StepVerifier.create(resultado)
                .expectError(
                        ProductoNoEncontradoException.class
                )
                .verify();
    }

    private List<ProductoEntity>
    datosConTresValidosYDosInvalidos() {

        return List.of(
                crearProducto(
                        "Café arábigo de altura",
                        "18.50",
                        "ventas@agrosmart.ec"
                ),
                crearProducto(
                        "Café especial de Loja",
                        "22.75",
                        "comercial@agrosmart.ec"
                ),
                crearProducto(
                        "Café tostado artesanal",
                        "16.90",
                        "pedidos@agrosmart.ec"
                ),
                crearProducto(
                        "Café experimental sin precio",
                        "0.00",
                        "evaluacion@agrosmart.ec"
                ),
                crearProducto(
                        "Café robusta natural",
                        "14.25",
                        ""
                )
        );
    }

    private ProductoEntity crearProducto(
            String nombre,
            String precio,
            String correos) {

        return new ProductoEntity(
                nombre,
                new BigDecimal(precio),
                100,
                "Café",
                correos
        );
    }
}
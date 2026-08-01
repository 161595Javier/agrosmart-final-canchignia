package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.domain.ProductoFilters;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.mapper.ProductoMapper;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private static final Producto PRODUCTO_GENERICO =
            new Producto(
                    0L,
                    "PRODUCTO GENÉRICO",
                    "Café",
                    new BigDecimal("1.00"),
                    List.of("soporte@agrosmart.ec")
            );

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Flux<Producto> obtenerProductosComercializables() {

        // fromCallable difiere la consulta bloqueante hasta la suscripción.
        return Mono.fromCallable(repository::findAll)

                // JPA/Hibernate bloquea; boundedElastic evita bloquear Netty.
                .subscribeOn(Schedulers.boundedElastic())

                // Convierte Mono<List<ProductoEntity>> en Flux<ProductoEntity>.
                .flatMapMany(Flux::fromIterable)

                // Transforma la entidad JPA en el dominio inmutable.
                .map(ProductoMapper::toDominio)

                // Crea una nueva instancia con el nombre en mayúsculas.
                .map(ProductoFilters.A_MAYUSCULAS)

                // Descarta precio cero o productos sin correos.
                .filter(ProductoFilters.IS_VALID)

                // Registra el producto sin modificarlo.
                .doOnNext(ProductoFilters.LOG_PRODUCTO)

                // Emite un producto genérico si no queda ninguno.
                .defaultIfEmpty(PRODUCTO_GENERICO);
    }

    public Mono<Producto> buscarPorId(Long id) {

        // findById es bloqueante y devuelve Optional.
        return Mono.fromCallable(() -> repository.findById(id))

                // Ejecuta la consulta fuera del event loop de Netty.
                .subscribeOn(Schedulers.boundedElastic())

                // Optional vacío se convierte en Mono vacío.
                .flatMap(optional -> Mono.justOrEmpty(optional))

                // Convierte la entidad encontrada al dominio inmutable.
                .map(ProductoMapper::toDominio)

                // Cambia el Mono vacío por un error de negocio.
                .switchIfEmpty(
                        Mono.error(new ProductoNoEncontradoException(id))
                );
    }
}
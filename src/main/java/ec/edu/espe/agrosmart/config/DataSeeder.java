package ec.edu.espe.agrosmart.config;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner sembrarProductos(ProductoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(

                        // Producto válido 1
                        new ProductoEntity(
                                "Café arábigo de altura",
                                new BigDecimal("18.50"),
                                120,
                                "Café",
                                "ventas@agrosmart.ec"
                        ),

                        // Producto válido 2
                        new ProductoEntity(
                                "Café especial de Loja",
                                new BigDecimal("22.75"),
                                85,
                                "Café",
                                "comercial@agrosmart.ec"
                        ),

                        // Producto válido 3
                        new ProductoEntity(
                                "Café tostado artesanal",
                                new BigDecimal("16.90"),
                                150,
                                "Café",
                                "pedidos@agrosmart.ec,ventas@agrosmart.ec"
                        ),

                        // Producto inválido: precio igual a cero
                        new ProductoEntity(
                                "Café experimental sin precio",
                                new BigDecimal("0.00"),
                                40,
                                "Café",
                                "evaluacion@agrosmart.ec"
                        ),

                        // Producto inválido: lista de correos vacía
                        new ProductoEntity(
                                "Café robusta natural",
                                new BigDecimal("14.25"),
                                70,
                                "Café",
                                ""
                        )
                ));
            }
        };
    }
}
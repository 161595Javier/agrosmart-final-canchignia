package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductoTest {

    @Test
    void getters_conDatosValidos_debenDevolverLosValores() {
        // Arrange
        List<String> correos = List.of("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Café arábigo de altura",
                "Café",
                new BigDecimal("18.50"),
                correos
        );

        // Act & Assert
        assertEquals(1L, producto.getId());
        assertEquals("Café arábigo de altura", producto.getNombre());
        assertEquals("Café", producto.getCategoria());
        assertEquals(new BigDecimal("18.50"), producto.getPrecioUsd());
        assertEquals(correos, producto.getCorreosNotificacion());
    }

    @Test
    void constructor_alModificarListaOriginal_noDebeModificarProducto() {
        // Arrange
        List<String> correosOriginales = new ArrayList<>();
        correosOriginales.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Café especial de Loja",
                "Café",
                new BigDecimal("22.75"),
                correosOriginales
        );

        // Act
        correosOriginales.add("intruso@correo.com");

        // Assert
        assertEquals(1, producto.getCorreosNotificacion().size());
        assertEquals(
                "ventas@agrosmart.ec",
                producto.getCorreosNotificacion().getFirst()
        );
    }

    @Test
    void getCorreosNotificacion_alIntentarModificar_debeLanzarExcepcion() {
        // Arrange
        List<String> correosOriginales = new ArrayList<>();
        correosOriginales.add("pedidos@agrosmart.ec");

        Producto producto = new Producto(
                2L,
                "Café tostado artesanal",
                "Café",
                new BigDecimal("16.90"),
                correosOriginales
        );

        // Act
        List<String> correosDevueltos =
                producto.getCorreosNotificacion();

        // Assert
        assertNotSame(
                correosOriginales,
                correosDevueltos
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> correosDevueltos.add("nuevo@correo.com")
        );
    }
}
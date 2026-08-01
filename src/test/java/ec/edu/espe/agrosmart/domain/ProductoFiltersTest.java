package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoFiltersTest {

    @Test
    void isValid_conPrecioPositivoYCorreo_debeRetornarTrue() {
        // Arrange
        Producto producto = new Producto(
                1L,
                "Café arábigo de altura",
                "Café",
                new BigDecimal("18.50"),
                List.of("ventas@agrosmart.ec")
        );

        // Act
        boolean resultado =
                ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void isValid_conPrecioCero_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                2L,
                "Café experimental sin precio",
                "Café",
                new BigDecimal("0.00"),
                List.of("evaluacion@agrosmart.ec")
        );

        // Act
        boolean resultado =
                ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void isValid_sinCorreos_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                3L,
                "Café robusta natural",
                "Café",
                new BigDecimal("14.25"),
                List.of()
        );

        // Act
        boolean resultado =
                ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void aMayusculas_conProductoValido_debeCrearNuevaInstancia() {
        // Arrange
        Producto productoOriginal = new Producto(
                4L,
                "Café especial de Loja",
                "Café",
                new BigDecimal("22.75"),
                List.of("comercial@agrosmart.ec")
        );

        // Act
        Producto productoTransformado =
                ProductoFilters.A_MAYUSCULAS.apply(productoOriginal);

        // Assert
        assertNotSame(productoOriginal, productoTransformado);
        assertTrue(
                productoTransformado.getNombre()
                        .equals("CAFÉ ESPECIAL DE LOJA")
        );
        assertTrue(
                productoOriginal.getNombre()
                        .equals("Café especial de Loja")
        );
    }
}
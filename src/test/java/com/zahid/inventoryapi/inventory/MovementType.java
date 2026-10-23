package com.zahid.inventoryapi.inventory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

class MovementTypeTest {

    @Test
    void entrada_sumaLaCantidadAlStock() {
        int currentStock = 10;
        int quantity = 5;

        int result = MovementType.ENTRADA.applyTo(currentStock, quantity);

        assertThat(result).isEqualTo(15);
    }

    @ParameterizedTest(name = "{0}: stock {1} y cantidad {2} dan {3}")
    @CsvSource({
            "ENTRADA, 10, 5, 15",
            "SALIDA, 10, 3, 7",
            "AJUSTE, 10, 4, 14",
            "AJUSTE, 10, -6, 4",
            "SALIDA, 5, 8, -3",
            "ENTRADA, 10, 0, 10",
            "AJUSTE, 10, 0, 10"
    })
    void applyTo_calculaElNuevoStockSegunElTipo(MovementType type, int stock, int quantity, int expected) {
        assertThat(type.applyTo(stock, quantity)).isEqualTo(expected);
    }
}
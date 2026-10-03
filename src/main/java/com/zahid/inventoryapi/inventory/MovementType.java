package com.zahid.inventoryapi.inventory;

public enum MovementType {

    ENTRADA {
        @Override
        public int applyTo(int currentStock, int quantity) {
            return currentStock + quantity;
        }
    },

    SALIDA {
        @Override
        public int applyTo(int currentStock, int quantity) {
            return currentStock - quantity;
        }
    },

    AJUSTE {
        @Override
        public int applyTo(int currentStock, int quantity) {
            return currentStock + quantity;
        }
    };

    public abstract int applyTo(int currentStock, int quantity);
}
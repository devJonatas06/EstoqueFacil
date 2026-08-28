package com.example.EstoqueFacil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public enum StockMovementType {
    ENTRY,
    SALE,
    LOSS;
}
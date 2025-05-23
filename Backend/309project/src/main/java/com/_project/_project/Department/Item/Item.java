package com._project._project.Department.Item;

import com._project._project.Department.Department;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Item {

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String itemName;
    private BigDecimal price;
    private BigDecimal cost;
    private String description;
    private boolean isAvailable;

    @Min(0)
    private int quantity;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] itemImage;

    public Item(String itemName, BigDecimal price, BigDecimal cost, int quantity, String description) {
        this.itemName = itemName;
        this.price = price;
        this.cost = cost;
        this.description = description;
        this.quantity = quantity;
        this.isAvailable = quantity > 0;
    }
}

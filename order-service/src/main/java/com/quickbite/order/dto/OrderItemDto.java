package com.quickbite.order.dto;

import lombok.Data;

// Represents one item from the cart sent by Angular when placing an order
@Data
public class OrderItemDto {

    private Long menuItemId;
    private String itemName;
    private Double itemPrice;
    private Integer quantity;
    private Double subtotal;
    private String customization;
}
package com.ecommerce.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class DummyJsonRequest {

    private String title;

    private String category;

    private Double price;

    private Double rating;

    private String brand;

    private List<String> tags;

    private String thumbnail;

    private Long stock;
}
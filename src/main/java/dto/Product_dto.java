package dto;

import lombok.Data;

@Data
public class Product_dto {
    private Long id;
    private String imageName;
    private String name;
    private double price;
    private Boolean productAvailable;
    private String category;
}

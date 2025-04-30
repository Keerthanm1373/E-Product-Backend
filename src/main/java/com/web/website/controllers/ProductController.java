package com.web.website.controllers;

import com.web.website.models.Products;
import com.web.website.services.ProductService;
import dto.Product_dto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/web")
public class ProductController {
    @Autowired
    private ProductService service;


    @GetMapping("/product")
    public List<Product_dto> getProducts() {
        return service.getProduct();
    }

    @GetMapping("/product/{id}")
    public Optional<Products> getProductId(@PathVariable Long id){
        return service.getProductId(id);
    }
    @PutMapping("/update")
    public Products updateProduct(@RequestPart Products product) {
        return service.updateProduct(product);
    }

    @GetMapping("/getproduct")
    public List<Products> getAllProduct(){
        return service.getAllProduct();
    }

    @PostMapping( "/products")
    public ResponseEntity<?> addProduct(@RequestPart Products product,
                                        @RequestPart MultipartFile imageFile) {
        try{
            System.out.println(product);
            Products product1 = service.saveProduct(product, imageFile);
            return new ResponseEntity<>(product1, HttpStatus.CREATED);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/fetch-products")
    public ResponseEntity<String> fetchProducts(@RequestParam String searchQuery, @RequestParam String productTitle) {
        try {
            service.fetchAndSaveProductsFromAPI(searchQuery, productTitle);
            return ResponseEntity.ok("Product fetched and saved.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

package org.paymentchain.products.controller;

import org.paymentchain.products.entity.Product;
import org.paymentchain.products.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    @GetMapping
    public ResponseEntity<List<Product>> getAll(){

        List<Product> products = repository.findAll();
        if (products.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id){

        Product product = repository.findById(id).orElse(null);
        if (product == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product){

        Product newProduct = repository.save(product);
        return ResponseEntity.ok(newProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){

        Product product = repository.findById(id).orElse(null);
        if (product == null){
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return (ResponseEntity<?>) ResponseEntity.ok();
    }

}

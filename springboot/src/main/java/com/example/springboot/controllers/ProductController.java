package com.example.springboot.controllers;

import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;


import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;




import java.util.*;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Products API", description = "Products management API")
@RestController
public class ProductController {

    @Autowired
    ProductRepository productRepository;


    @Operation(
            summary = "Create a Product in database.",
            description = "Create a Product object by specifying the name and value in JSON format. You can create more than one per request. The response is Product object with id, name and value.",
            tags = { "products", "post" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProductController.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })

    @PostMapping("/products")
    public ResponseEntity<List<ProductModel>> saveProduct(@RequestBody @Valid List<ProductRecordDto> productRecordDto)
    {
        List<ProductModel> savedProducts = new ArrayList<>();
        for (ProductRecordDto productDto : productRecordDto) {
            var productModel = new ProductModel();
            BeanUtils.copyProperties(productDto, productModel);
            savedProducts.add(productRepository.save(productModel));

        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProducts);
    }

    @Operation(
            summary = "Read all Products in database.",
            description = "Reads all products. The response is all Products objects with their id, name and value.",
            tags = { "products", "getall" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProductController.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "204", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/products")
    public ResponseEntity<Object> getAllProducts(Pageable pageable) {
        Page<ProductModel> productsPage = productRepository.findAll(pageable);

        if (productsPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        for (ProductModel product : productsPage) {
            UUID id = product.getIdProduct();
            product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
        }

        CollectionModel<ProductModel> pagedModel = PagedModel.of(productsPage.getContent()
        );
        pagedModel.add(linkTo(methodOn(ProductController.class).getAllProducts(pageable.next())).withRel("next"));
        pagedModel.add(linkTo(methodOn(ProductController.class).getAllProducts(pageable.previousOrFirst())).withRel("prev"));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(
            summary = "Read a Product in database.",
            description = "Get a Product object by specifying its ID. The response is Product object with id, name and value.",
            tags = { "products", "getone" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProductController.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value = "id") UUID id)
    {
        Optional<ProductModel> product0 = productRepository.findById(id);
        if (product0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This product was not found. Try again.");
        }
        product0.get().add(linkTo(methodOn(ProductController.class).getAllProducts(Pageable.unpaged())).withRel("Products List:"));
        return ResponseEntity.status(HttpStatus.OK).body(product0.get());
    }

    @Operation(
            summary = "Updates a Product in database.",
            description = "Updates a Product object by specifying its ID and new values in body with JSON format. The response is Product object with id, name and value.",
            tags = { "products", "put" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProductController.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id,
                                                @RequestBody @Valid ProductRecordDto productRecordDto)
    {
     Optional<ProductModel> product0 = productRepository.findById(id);
     if(product0.isEmpty()) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This product was not found. Try again.");
     }
     var productModel = product0.get();
     BeanUtils.copyProperties(productRecordDto, productModel);
     return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }

    @Operation(
            summary = "Delete a Product in database.",
            description = "Delete a Product object by specifying its ID. The response is Product object with id, name and value.",
            tags = { "products", "post" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProductController.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id)
    {
        Optional<ProductModel> product0 = productRepository.findById(id);
        if (product0.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This product was not found. Try again.");

        }
        productRepository.delete(product0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
    }
}

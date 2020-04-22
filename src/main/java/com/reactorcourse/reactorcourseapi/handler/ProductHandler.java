package com.reactorcourse.reactorcourseapi.handler;

import com.reactorcourse.reactorcourseapi.model.Product;
import com.reactorcourse.reactorcourseapi.model.ProductEvent;
import com.reactorcourse.reactorcourseapi.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * Created by Jonathan Tuta on 4/21/2020.
 * <p>
 * Requests Handled Asynchronously
 */

@Component
public class ProductHandler {
    private final ProductRepository productRepository;

    public ProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<ServerResponse> getAllProducts(ServerRequest serverRequest) {
        Flux<Product> products = productRepository.findAll();

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        Mono<Product> productMono = productRepository.findById(id);

        ///> Use flatMap or map to incorporate the use of switchIfEmpty and defaultIfEmpty
        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(product)))
                .switchIfEmpty(ServerResponse.notFound().build()); // similar to defaultIfEmpty used in ProductController,
        // defaultIfEmpty take a simple object as default while
        // switchIfEmpty take a publisher implementation
        // (takes Mono or Flux depending on what you're working on)
    }

    public Mono<ServerResponse> saveProduct(ServerRequest serverRequest) {
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);

        return productMono
                .flatMap(product ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(productRepository.save(product), Product.class));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        Mono<Product> existingProductMono = productRepository.findById(id);
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        ///> Combine multiple mono operators
        return productMono.zipWith(existingProductMono,
                (product, existingProduct) ->
                        new Product(existingProduct.getId(), product.getName(), product.getPrice())
        ).flatMap(product ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productRepository.save(product), Product.class)
        ).switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        Mono<Product> productMono = productRepository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .build(productRepository.delete(product))
                )
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteAllProducts(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .build(productRepository.deleteAll());
    }

    public Mono<ServerResponse> getProductEvents(ServerRequest serverRequest) {
        Flux<ProductEvent> productEventFlux = Flux.interval(Duration.ofSeconds(1))
                .map(val ->
                        new ProductEvent(val, "Product Event"));

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(productEventFlux, ProductEvent.class);
    }
}

package com.reactorcourse.reactorcourseapi.webclient;

import com.reactorcourse.reactorcourseapi.model.Product;
import com.reactorcourse.reactorcourseapi.model.ProductEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by Jonathan Tuta on 4/21/2020.
 */

public class WebClientAPI {
    private WebClient webClient;

    public WebClientAPI() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080/products")
                .build();
    }

    /**
     * First run the api (Spring Boot Project) then run main
     *
     * @param args
     */
    public static void main(String[] args) {
        WebClientAPI webClientAPI = new WebClientAPI();

        webClientAPI.postNewProduct()
                .thenMany(webClientAPI.getAllProducts())
                .take(1)
                .flatMap(p -> webClientAPI.updateProduct(p.getId(), "White Tea", 0.99))
                .flatMap(p -> webClientAPI.deleteProduct(p.getId()))
                .thenMany(webClientAPI.getAllProducts())
                .thenMany(webClientAPI.getAllEvents())
                .subscribe(System.out::println);
    }

    private Mono<ResponseEntity<Product>> postNewProduct() {
        return webClient
                .post()
                .body(Mono.just(new Product(null, "Black Tea", 1.99)), Product.class)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(Product.class))
                .doOnSuccess(o -> System.out.println("**************** POST: " + o));
    }

    private Flux<Product> getAllProducts() {
        return webClient
                .get()
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(o -> System.out.println("******************* GET: " + o));
    }

    /**
     * retrieve gives access to the body only while exchange give access to the complete response
     *
     * @param id
     * @param name
     * @param price
     * @return
     */
    private Mono<Product> updateProduct(String id, String name, double price) {
        return webClient
                .put()
                .uri("/{id}", id)
                .body(Mono.just(new Product(null, name, price)), Product.class)
                .retrieve() // to perform the request, if exchange used instead then map operation needed, see postNewProduct method above
                .bodyToMono(Product.class)
                .doOnSuccess(o -> System.out.println("*********************** UPDATE: " + o));
    }

    private Mono<Void> deleteProduct(String id) {
        return webClient
                .delete()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(o -> System.out.println("********************* DELETE: " + o));

    }

    private Flux<ProductEvent> getAllEvents() {
        return webClient
                .get()
                .uri("/events")
                .retrieve()
                .bodyToFlux(ProductEvent.class);
    }
}

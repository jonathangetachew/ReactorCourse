package com.reactorcourse.reactorcourseapi.controller;

import com.reactorcourse.reactorcourseapi.handler.ProductHandler;
import com.reactorcourse.reactorcourseapi.model.Product;
import com.reactorcourse.reactorcourseapi.model.ProductEvent;
import com.reactorcourse.reactorcourseapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ProductController.class) // By default configures beans with @Controller, @JsonComponent, @Converters, @WebFluxConfigurer
    // But not @Service, @Component or @Repository beans so those have to be mocked, can't be autowired
class ProductControllerTestJUnit5Annotation {

    @Autowired
    private WebTestClient webTestClient;

    private List<Product> expectedList;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private CommandLineRunner commandLineRunner;

    ///> Not needed in these tests but has to be included because it's a dependency for the context, check main class
    @MockBean
    private ProductHandler productHandler;

    @BeforeEach
    void setUp() {
        this.expectedList = Arrays.asList(
                new Product("1", "Big Latte", 2.99)
        );
    }

    @Test
    void getAllProducts() {
        when(productRepository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));
        webTestClient
                .get()
                .uri("/products") // Optional since our base url is the same
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }

    @Test
    void getProduct_invalidId_notFound() {
        String id = "xxx";
        when(productRepository.findById(id)).thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri("/products/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getProduct_validId_found() {
        Product expectedProduct = this.expectedList.get(0);
        when(productRepository.findById(expectedProduct.getId())).thenReturn(Mono.just(expectedProduct));

        webTestClient
                .get()
                .uri("/products/{id}", expectedProduct.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }

    @Test
    void getProductEvents() {
        ProductEvent expectedEvent = new ProductEvent(0L, "Product Event");

        FluxExchangeResult<ProductEvent> result =
                webTestClient.get().uri("/products/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(ProductEvent.class);

        ///> StepVerifier used here because webTestClient doesn't have a functionality to assert events as they come
        ///> or cancel the stream ones the test is done.
        StepVerifier.create(result.getResponseBody())
                .expectNext(expectedEvent)
                .expectNextCount(2)
                .consumeNextWith(productEvent ->
                        assertEquals(Long.valueOf(3), productEvent.getEventId()))  // event started with 0 so after 2 next will be 3
                .thenCancel()
                .verify();
    }

    @Test
    void saveProduct() {
    }

    @Test
    void updateProduct() {
    }

    @Test
    void deleteProduct() {
    }

    @Test
    void deleteAllProducts() {
    }
}
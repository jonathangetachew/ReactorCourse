package com.reactorcourse.reactorcourseapi.controller;

import com.reactorcourse.reactorcourseapi.model.Product;
import com.reactorcourse.reactorcourseapi.model.ProductEvent;
import com.reactorcourse.reactorcourseapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ProductControllerTestJUnit5ApplicationContext {

    private WebTestClient webTestClient;

    private List<Product> expectedList;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient =
                WebTestClient
                        .bindToApplicationContext(applicationContext)// autowiring the controller and directly passing it also possible
                        .configureClient()
                        .baseUrl("/products")
                        .build();

        expectedList = productRepository.findAll().collectList().block(); // block converts asynchronous call to synchronous
    }

    @Test
    void getAllProducts() {
        webTestClient
                .get()
                .uri("/") // Optional since our base url is the same
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }

    @Test
    void getProduct_invalidId_notFound() {
        webTestClient
                .get()
                .uri("/xxx")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getProduct_validId_found() {
        Product expectedProduct = expectedList.get(0);
        webTestClient
                .get()
                .uri("/{id}", expectedProduct.getId())
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
                webTestClient.get().uri("/events")
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
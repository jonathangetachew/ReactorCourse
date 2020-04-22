package com.reactorcourse.reactorcourseapi.controller;

import com.reactorcourse.reactorcourseapi.model.Product;
import com.reactorcourse.reactorcourseapi.model.ProductEvent;
import com.reactorcourse.reactorcourseapi.repository.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductControllerTestJUnit4 {

    private WebTestClient webTestClient;

    private List<Product> expectedList;

    @Autowired
    private ProductRepository productRepository;

    @Before
    public void setUp() {
        webTestClient =
                WebTestClient
                        .bindToController(new ProductController(productRepository))// autowiring the controller and directly passing it also possible
                        .configureClient()
                        .baseUrl("/products")
                        .build();

        expectedList = productRepository.findAll().collectList().block(); // block converts asynchronous call to synchronous
    }

    @Test
    public void getAllProducts() {
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
    public void getProduct_invalidId_notFound() {
        webTestClient
                .get()
                .uri("/xxx")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void getProduct_validId_found() {
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
    public void getProductEvents() {
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
    public void saveProduct() {
    }

    @Test
    public void updateProduct() {
    }

    @Test
    public void deleteProduct() {
    }

    @Test
    public void deleteAllProducts() {
    }
}
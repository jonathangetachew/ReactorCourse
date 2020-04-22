package com.reactorcourse.reactorcourseapi;

import com.reactorcourse.reactorcourseapi.handler.ProductHandler;
import com.reactorcourse.reactorcourseapi.model.Product;
import com.reactorcourse.reactorcourseapi.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class ReactorCourseApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactorCourseApiApplication.class, args);
    }

    @Bean
    CommandLineRunner init(/*ReactiveMongoOperations operations,*/ ProductRepository productRepository) {
        return args -> {
            Flux<Product> productFlux = Flux.just(
                    new Product(null, "Big Latte", 2.99),
                    new Product(null, "Big Decaf", 2.49),
                    new Product(null, "Green Tea", 1.99)
            ).flatMap(productRepository::save);

            productFlux
                    .thenMany(productRepository.findAll())
                    .subscribe(System.out::println);

//			operations.collectionExists(Product.class)
//					.flatMap(exists -> exists ? operations.dropCollection(Product.class) : Mono.just(exists))
//					.thenMany(v -> operations.createCollection(Product.class))
//					.thenMany(productFlux)
//					.thenMany(productRepository.findAll())
//					.subscribe(System.out::println);

        };
    }

    /**
     * The order of the route definitions isn't random, if GET-events is defined below GET-product then the GET-product
     * route will be called with 'event' as pathVariable every time.
     *
     * @param productHandler
     * @return
     */
    @Bean
    RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
//        return route(GET("/functional-products").and(accept(MediaType.APPLICATION_JSON)), productHandler::getAllProducts)
//                .andRoute(POST("/functional-products").and(contentType(MediaType.APPLICATION_JSON)), productHandler::saveProduct)
//                .andRoute(DELETE("/functional-products").and(accept(MediaType.APPLICATION_JSON)), productHandler::deleteAllProducts)
//                .andRoute(GET("/functional-products/events").and(accept(MediaType.TEXT_EVENT_STREAM)), productHandler::getProductEvents)
//                .andRoute(GET("/functional-products/{id}").and(accept(MediaType.APPLICATION_JSON)), productHandler::getProduct)
//                .andRoute(PUT("/functional-products/{id}").and(contentType(MediaType.APPLICATION_JSON)), productHandler::updateProduct)
//                .andRoute(DELETE("/functional-products/{id}").and(accept(MediaType.APPLICATION_JSON)), productHandler::deleteProduct);
        return nest(path("/functional-products"),
                nest(accept(MediaType.APPLICATION_JSON).or(contentType(MediaType.APPLICATION_JSON)).or(accept(MediaType.TEXT_EVENT_STREAM)),
                        route(GET("/"), productHandler::getAllProducts)
                                .andRoute(method(HttpMethod.POST), productHandler::saveProduct)
                                .andRoute(DELETE("/"), productHandler::deleteAllProducts)
                                .andRoute(GET("/events"), productHandler::getProductEvents)
                                .andNest(path("/{id}"),
                                        route(method(HttpMethod.GET), productHandler::getProduct)
                                                .andRoute(method(HttpMethod.PUT), productHandler::updateProduct)
                                                .andRoute(method(HttpMethod.DELETE), productHandler::deleteProduct)
                                )
                )
        );
    }
}

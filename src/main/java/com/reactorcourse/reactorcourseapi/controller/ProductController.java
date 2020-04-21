package com.reactorcourse.reactorcourseapi.controller;

import com.reactorcourse.reactorcourseapi.model.Product;
import com.reactorcourse.reactorcourseapi.model.ProductEvent;
import com.reactorcourse.reactorcourseapi.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Created by Jonathan on 4/20/2020.
 */

@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductRepository productRepository;

	public ProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@GetMapping
	public Flux<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id) {
		return productRepository.findById(id)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	/**
	 * Specify TEXT_EVENT_STREAM_VALUE to avoid returning a flux of server side event, To return ProductEvent
	 */
	@GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ProductEvent> getProductEvents() {
		return Flux.interval(Duration.ofSeconds(1))
				.map(val ->
						new ProductEvent(val, "Product Event"));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Product> saveProduct(@RequestBody Product product) {
		return productRepository.save(product);
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<Product>> updateProduct(@PathVariable String id, @RequestBody Product product) {
		return productRepository.findById(id)
				.flatMap(existingProduct -> {
					existingProduct.setName(product.getName());
					existingProduct.setPrice(product.getPrice());
					return productRepository.save(existingProduct);
				})
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
		return productRepository.findById(id)
				.flatMap(existingProduct ->
						productRepository.delete(existingProduct)
								.then(Mono.just(ResponseEntity.ok().<Void>build()))
				).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping
	public Mono<Void> deleteAllProducts() {
		return productRepository.deleteAll();
	}
}

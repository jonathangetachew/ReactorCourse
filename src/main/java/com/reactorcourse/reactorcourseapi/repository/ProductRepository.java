package com.reactorcourse.reactorcourseapi.repository;

import com.reactorcourse.reactorcourseapi.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Jonathan on 4/20/2020.
 */

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}

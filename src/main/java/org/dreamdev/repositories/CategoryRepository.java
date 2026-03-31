package org.dreamdev.repositories;

import org.dreamdev.models.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.net.http.HttpHeaders;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findByCategoryId(String categoryId);
}

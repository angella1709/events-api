package com.example.angella.eventsapi.repository;

import com.example.angella.eventsapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByNameIn(Collection<String> names);

}

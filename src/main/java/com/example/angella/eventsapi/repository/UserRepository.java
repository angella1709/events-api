package com.example.angella.eventsapi.repository;

import com.example.angella.eventsapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT DISTINCT u.email FROM User u " +
            "LEFT JOIN u.subscribedCategories c " +
            "WHERE c.id IN :categoryIds")
    Set<String> getEmailsBySubscriptions(@Param("categoryIds") Collection<Long> categoriesId);


    boolean existsByIdAndSubscribedCategoriesId(Long userId, Long categoryId);

    boolean existsByUsernameOrEmail(String username, String email);
}

package com.example.angella.eventsapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Category extends BaseEntity {

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "categories")
    private Set<Event> events = new HashSet<>();

    public Category(Object o, @NotBlank(message = "Category must not be blank!") String categoryName, Object o1) {
    }
}

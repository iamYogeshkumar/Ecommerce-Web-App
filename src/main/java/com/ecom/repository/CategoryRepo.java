package com.ecom.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Category;

public interface CategoryRepo extends JpaRepository<Category, Integer> {

	public boolean existsByName(String name);

	public List<Category> findByisActiveTrue();
}

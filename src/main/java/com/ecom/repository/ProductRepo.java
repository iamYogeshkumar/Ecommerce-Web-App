package com.ecom.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Product;

public interface ProductRepo extends JpaRepository<Product, Integer> {

	public List<Product> findByIsActiveTrue();
	 
	List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch,String ch2);

	public Page<Product> findByIsActiveTrue(Pageable of);

	//public Page<Product> findByCategory(Pageable of);

	public Page<Product> findByCategoryAndIsActiveTrue(String category,Pageable of);
	
	Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch,String ch2,Pageable p);

	public Page<Product> findByIsActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch,
			String ch2, PageRequest of);
}

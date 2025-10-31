package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

	public List<Cart> findByUserId(int userId);

	public Cart findByProductIdAndUserId(int productId , int userId);

    public Integer countByUserId(int userId);
	
}

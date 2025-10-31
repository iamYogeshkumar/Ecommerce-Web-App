package com.ecom.service;

import java.util.List;

import com.ecom.model.Cart;

public interface CartService {

	public Cart saveCart(Integer productId,Integer userId);
	
	public List<Cart> getCartByUser(Integer userId);
	
	public Integer getCountcart(Integer userId);
	
	public void increaseQuantity(int pid);

	public void decreaseQuantity(int cartId);
}

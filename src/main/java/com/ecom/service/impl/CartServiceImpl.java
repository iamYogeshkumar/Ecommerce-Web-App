package com.ecom.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductRepo;
import com.ecom.repository.UserDtlsRepo;
import com.ecom.service.CartService;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepo;
	
	@Autowired
	private UserDtlsRepo userRepo;
	
	@Autowired
	private ProductRepo productRepo;

	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		UserDtls user = userRepo.findById(userId).get();
		Product product = productRepo.findById(productId).get();
		Cart cartStatus = cartRepo.findByProductIdAndUserId(productId, userId);
		Cart cart=new Cart();
		if(ObjectUtils.isEmpty(cartStatus)) {
			cart.setUser(user);
			cart.setProduct(product);
			cart.setQuantity(1);
			cart.setTotalPrice(1*product.getDiscountPrice());
			
		}else {
			cart=cartStatus;
			cart.setQuantity(cartStatus.getQuantity()+1);
			cart.setTotalPrice(cartStatus.getQuantity()*cartStatus.getProduct().getDiscountPrice());
		}
		
		return cartRepo.save(cart);
	}

	@Override
	public List<Cart> getCartByUser(Integer userId) {
		 List<Cart> carts = cartRepo.findByUserId(userId);
		double sum=0.0;
		 for(Cart c:carts) {
			 Double totalPrice=0.0;
			 Integer quantity = c.getQuantity();
			 Double price = c.getProduct().getDiscountPrice();
			 totalPrice=quantity*price;
			 c.setTotalPrice(totalPrice);
			
		 }
		 return carts;
	}

	@Override
	public Integer getCountcart(Integer userId) {
		Integer countByUserId = cartRepo.countByUserId( userId);
		return countByUserId;
	}

	@Override
	public void increaseQuantity(int cartId) {
		System.out.println("increase Quantity");
		
		Optional<Cart> byId = cartRepo.findById(cartId);
		if(byId.isPresent()) {
			Cart cart = byId.get();
			cart.setQuantity(cart.getQuantity()+1);
			System.out.println(cart.getQuantity());
			cartRepo.save(cart);
		}
		System.out.println("Increse Quantity closed");
	}

	@Override
	public void decreaseQuantity(int cartId) {
		System.out.println("decrease Quantity start");
		Cart cart = cartRepo.findById(cartId).orElse(null);
		if(!ObjectUtils.isEmpty(cart)) {
			if(cart.getQuantity()>1) {
				cart.setQuantity(cart.getQuantity()-1);
				cartRepo.save(cart);
			}
			else {
				cartRepo.deleteById(cartId);
			}
			
		}
		
		
		
		System.out.println("decrease Quantity start");
		
		
	}
	
	

}

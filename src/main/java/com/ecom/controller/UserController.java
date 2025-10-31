package com.ecom.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.ProductRepo;
import com.ecom.repository.UserDtlsRepo;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private CategoryService categiryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private UserDtlsRepo userRepo;

	@GetMapping("/")
	public String home() {

		return "user/home";
	}

	@Autowired
	private UserService userService;

	@ModelAttribute
	public void getUserDeatils(Principal p, Model m) {
		if (!ObjectUtils.isEmpty(p)) {
			String email = p.getName();
			UserDtls user = userService.getUserByEmail(email);
			m.addAttribute("countCart", cartService.getCountcart(user.getId()));
			;
			m.addAttribute("user", user);
		}
		m.addAttribute("category", categiryService.getAllActiveCategory());

	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam int pid, @RequestParam int uid, HttpSession session) {
		Cart saveCart = cartService.saveCart(pid, uid);
		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Add to cart failed");
		} else {
			session.setAttribute("successMsg", "product is added to cart");
		}
		return "redirect:/product/" + pid;
	}

	/*
	 * @GetMapping("/cart") public String loadCartPage(@RequestParam int uid,Model
	 * m) { List<Cart> cartByUser = cartService.getCartByUser(uid);
	 * m.addAttribute("carts", cartByUser);
	 * 
	 * return "/user/cart"; }
	 */

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {
		UserDtls user = getLoggedInUserDetails(p);

		List<Cart> carts = cartService.getCartByUser(user.getId());
		// #21 27:57
		Double totalOrderAmt = totalAmt(carts);
		m.addAttribute("carts", carts);
		
		m.addAttribute("orderAmt", totalOrderAmt);
	
		
		return "/user/cart";
	}

	@GetMapping("/addQuantity") //cart quantity increase
	public String addQuantity(@RequestParam int cartId) {
		cartService.increaseQuantity(cartId);
		return "redirect:/user/cart";
	}
	
	@GetMapping("/decreaseQuantity") // cart quantity  decrease
	public String decreaseQuantity(@RequestParam int cartId) {
		cartService.decreaseQuantity(cartId);
		return "redirect:/user/cart";
	}
	
	
	private UserDtls getLoggedInUserDetails(Principal p) {

		String email = p.getName();

		UserDtls user = userRepo.findByEmail(email);
		
       return user;
	}
	
	private Double totalAmt(List<Cart> carts) {
	    if (carts == null || carts.isEmpty()) {
	        return 0.0;
	    }

	    double totalOrderPrice = 0.0;

	    for (Cart cart : carts) {
	        if (cart != null && cart.getTotalPrice() != null) {
	            totalOrderPrice += cart.getTotalPrice();
	        }
	    }

	    return totalOrderPrice;
	}

}

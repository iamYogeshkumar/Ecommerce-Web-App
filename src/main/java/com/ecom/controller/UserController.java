package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.UserDtlsRepo;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductOrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private CategoryService categiryService;

	@Autowired
	private CartService cartService;

	
	@Autowired
	private UserDtlsRepo userRepo;

	@Autowired
	private ProductOrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

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

	@GetMapping("/addQuantity") // cart quantity increase
	public String addQuantity(@RequestParam int cartId) {
		cartService.increaseQuantity(cartId);
		return "redirect:/user/cart";
	}

	@GetMapping("/decreaseQuantity") // cart quantity decrease
	public String decreaseQuantity(@RequestParam int cartId) {
		cartService.decreaseQuantity(cartId);
		return "redirect:/user/cart";
	}

	@GetMapping("/order")
	public String orderPage(Model m, Principal p) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartByUser(user.getId());
		Double totalAmt = totalAmt(carts);
		m.addAttribute("totalPrice", totalAmt);
		int tax = 30;
		int deliveryFee = 40;
		double finalPrice = tax + deliveryFee + totalAmt;
		m.addAttribute("finalPrice", finalPrice);
		return "/user/order";
	}

	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest orderRequest, Principal p) {

		UserDtls user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), orderRequest);

		return "redirect:/user/success";
	}

	@GetMapping("/success")
	public String loadSuccess() {

		return "/user/success";
	}

	@GetMapping("/user-orders")
	public String myOrders(Model m, Principal p) {
		UserDtls user = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrdersByUser(user.getId());
		m.addAttribute("orders", orders);
		return "/user/my_order";
	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();

		String status = "";
		for (OrderStatus orderst : values) {
			if (orderst.getId().equals(st)) {
				status = orderst.getName();
			}
		}
		// status=OrderStatus.CANCLE.getName();
		ProductOrder updateOrderStatus = orderService.updateOrderStatus(id, status);
		commonUtil.sendMAilForProductOrder(updateOrderStatus);
		if (!ObjectUtils.isEmpty(updateOrderStatus)) {
			session.setAttribute("successMsg", "Order Cancelled");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/user/user-orders";
	}

	@GetMapping("/profile")
	public String profile() {

		return "/user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile profileImage,
			HttpSession session) throws Exception {
		UserDtls updateUserProfile = userService.updateUserProfile(user, profileImage);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Something wrong on server");
		} else {
			session.setAttribute("successMsg", "updated successfully");
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,HttpSession session
			) {
		UserDtls userDetails = getLoggedInUserDetails(p);
		boolean matches = passwordEncoder.matches(currentPassword, userDetails.getPassword());
		
		if (matches) {
			String encode = passwordEncoder.encode(newPassword);
			userDetails.setPassword(encode);
			UserDtls updateUser = userService.updateUser(userDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", " Password not updated !! error in server  ");

			}else {
				session.setAttribute("successMsg", "password updated successfully");

			}
		}else {
			session.setAttribute("errorMsg", "Current Password is wrong ");

		}
			return "redirect:/user/profile";
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

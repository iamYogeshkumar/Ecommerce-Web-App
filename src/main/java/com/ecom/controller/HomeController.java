package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categiryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private CartService cartService;

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@ModelAttribute
	public void getUserDeatils(Principal p, Model m) {

		if (!ObjectUtils.isEmpty(p)) {
			String email = p.getName();
			UserDtls user = userService.getUserByEmail(email);
			 m.addAttribute("countCart",cartService.getCountcart(user.getId()));
			m.addAttribute("user", user);
		}
		m.addAttribute("category", categiryService.getAllActiveCategory());

	}

	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
		List<Category> categories = categiryService.getAllActiveCategory();
		List<Product> products = productService.getAllActiveProducts();

		if (category.equals("")) {
			m.addAttribute("products", products);
			m.addAttribute("categories", categories);
		} else {
			List<Product> filterProduct = products.stream().filter(i -> i.getCategory().equals(category))
					.collect(Collectors.toList());
			m.addAttribute("products", filterProduct);
			m.addAttribute("categories", categories);
		}
		m.addAttribute("paramValue", category);
		return "product";
	}

	@GetMapping("/product/{id}")
	public String viewProduct(@PathVariable int id, Model m) {
		Product product = productService.editProductById(id);
		m.addAttribute("product", product);
		return "view_product";
	}

	@PostMapping("/saveUser")
	public String saverUser(@ModelAttribute UserDtls user, HttpSession session,
			@RequestParam("img") MultipartFile file) {

		boolean checkUser = userService.checkUser(user.getEmail());
		if (!checkUser) {
			String imgName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
			user.setProfileImage(imgName);
			UserDtls saveUser = userService.saveUser(user);

			if (!ObjectUtils.isEmpty(saveUser)) {
				if (!file.isEmpty()) {
					File saveFile;
					try {
						saveFile = new ClassPathResource("static/img/").getFile();
						Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img"
								+ File.separator + saveUser.getProfileImage());

						System.out.println(path);
						Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

					} catch (IOException e) {

						e.printStackTrace();
					}

				}
				session.setAttribute("successMsg", "Registration Successful");
			} else {
				session.setAttribute("errorMsg", "Registration Failed");

			}

		} else {
			session.setAttribute("errorMsg", "Email Already exist");
		}

		return "redirect:/register";
	}

	// forget pwd

	@GetMapping("/forget-password")
	public String showForgetPassword() {

		return "forget_password";
	}

	// reset pwd
	@GetMapping("/reset-password")  //url is in string format
	public String showresetPassword(@RequestParam String token,Model m) {
		UserDtls user = userService.getUserByToken(token);
		if (!ObjectUtils.isEmpty(user)) {
			m.addAttribute("token", token);
		} else {
			//session.setAttribute("errorMsg", "Your link is expired");
			m.addAttribute("msg","Your link is expired or invalid");
			return "message";
		}
		return "reset_password";
	}
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token,@RequestParam String password,Model m) {
		UserDtls user = userService.getUserByToken(token);
		System.out.println("THE TOKEN : "+token);
		if(user==null) {
			m.addAttribute("msg","Your link is expired or invalid");
			return "message";
		}else {
			userService.updateUserPwd(token, password);
			m.addAttribute("msg", "Your password Changed successfully");
		}
		return "message";
	}

	@PostMapping("/forget-password")
	public String processForgetPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
			throws Exception {
		UserDtls user = userService.getUserByEmail(email);
		if (ObjectUtils.isEmpty(user)) {
			session.setAttribute("errorMsg", "Email does not exist");
		} else {
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			// generate url="http://localhost:8080/reset-password?token="+resetToken;
			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;

			boolean sendMail = commonUtil.sendMail(email, url, user);
			if (sendMail) {
				session.setAttribute("successMsg", "Please check your mail... Password reset link sent to your email");
			} else {
				session.setAttribute("errorMsg", "something wrong on server");
			}

		}
		return "redirect:/forget-password";
	}

}

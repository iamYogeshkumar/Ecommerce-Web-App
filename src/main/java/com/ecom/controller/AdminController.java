package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartService cartService;

	@GetMapping("/")
	public String index() {
		return "admin/index";
	}
	
	@ModelAttribute
	public void getUserDeatils(Principal p,Model m) {
		
		if(!ObjectUtils.isEmpty(p)) {
			String email = p.getName();
			UserDtls user = userService.getUserByEmail(email);
			m.addAttribute("user", user);
			 m.addAttribute("countCart",cartService.getCountcart(user.getId()));;

		}
		m.addAttribute("category", categoryService.getAllActiveCategory());

		
	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> allCategory = categoryService.getAllCategory();
		List<String> categoryList = allCategory.parallelStream().map(i -> i.getName()).distinct()
				.collect(Collectors.toList());
		m.addAttribute("categories", categoryList);
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category(Model m) {
		m.addAttribute("categorys", categoryService.getAllCategory());
		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String addCategory(@ModelAttribute Category c, @RequestParam MultipartFile file, HttpSession session)
			throws Exception {
		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		c.setImageName(imageName);

		if (categoryService.existCategory(c.getName())) {

			session.setAttribute("errorMsg", "Category Name already exist");

		} else {

			Category saveCategory = categoryService.saveCategory(c);
			if (ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "Internal server error");
			} else {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path p = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				System.out.println("Path : " + p.toString());
				Files.copy(file.getInputStream(), p, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("successMsg", "Saved successfully");
			}
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);
		if (deleteCategory) {
			session.setAttribute("successMsg", "item deleted successfuly ");
		} else {
			session.setAttribute("errorMsg", "internal server error");
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		Category categoryById = categoryService.getCategoryById(id);
		m.addAttribute("category", categoryById);
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category c, @RequestParam MultipartFile file, HttpSession session)
			throws IOException {
		Category oldCategory = categoryService.getCategoryById(c.getId());
		String imageName = file.isEmpty() ? c.getImageName() : file.getOriginalFilename();
		if (!ObjectUtils.isEmpty(oldCategory)) {
			oldCategory.setName(c.getName());
			oldCategory.setIsActive(c.getIsActive());
			oldCategory.setImageName(imageName);
		}

		Category saveCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(saveCategory)) {

			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path p = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				System.out.println("Path : " + p.toString());
				Files.copy(file.getInputStream(), p, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("successMsg", "Saved successfully");
			}

			session.setAttribute("successMsg", "Category updated successFully");
		} else {

			session.setAttribute("errorMsg", "internal server error");
		}

		return "redirect:/admin/loadEditCategory/" + c.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session) throws Exception {

		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		product.setImage(imageName);

		Product saveProduct = productService.saveProduct(product);
		if (!ObjectUtils.isEmpty(saveProduct)) {

			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths
					.get(saveFile.getAbsolutePath() + File.separator + "product_img/" + image.getOriginalFilename());
			System.out.println(path);
			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			session.setAttribute("successMsg", "Product add successfully");

		} else {
			session.setAttribute("errorMsg", "internal server error");
		}
		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProduct(Model m) {
		List<Product> products = productService.getAllProduct();
		m.addAttribute("products", products);
		return "admin/products";

	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = productService.deleteProductById(id);
		if (deleteCategory) {
			session.setAttribute("successMsg", "item deleted successfuly ");
		} else {
			session.setAttribute("errorMsg", "internal server error");
		}
		return "redirect:/admin/products";

	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		Product product = productService.editProductById(id);

		if (!ObjectUtils.isEmpty(product)) {

			m.addAttribute("products", product);

			List<Category> allCategory = categoryService.getAllCategory();

			m.addAttribute("categories", allCategory);
			m.addAttribute("product", product);
		}

		return "/admin/edit_product";

	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product p, @RequestParam("file") MultipartFile image,
			HttpSession session) {

		if (p.getDiscount() < 0 || p.getDiscount() > 100) {
			session.setAttribute("errorMsg", "invalid discount");
		} else {

			Product product = productService.updateProduct(p, image);

			if (!ObjectUtils.isEmpty(product)) {
				session.setAttribute("successMsg", "product updated successfully");
			} else {
				session.setAttribute("errorMsg", "Something wrong on server");
			}

		}

		return "redirect:/admin/editProduct/" + p.getId();

	}
	
	@GetMapping("/users")
	public String getAllUser(Model m) {
		List<UserDtls> users = userService.getUsers("ROLE_USER");
		m.addAttribute("users", users);
		return "/admin/users";
	}
	
	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam String status,int id,HttpSession session) {
		System.out.println(status);
		boolean userStatus = Boolean.parseBoolean(status);
		Boolean statusChange = userService.updateAccountStatus(id,userStatus);
		if(statusChange) {
			session.setAttribute("successMsg", "status updated sucesstfully");
		}
		else {
			session.setAttribute("errorMsg", "status updated failed");
		}
		return "redirect:/admin/users";
	}

}

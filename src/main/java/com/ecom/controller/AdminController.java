package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import com.ecom.binding.CategoryBinding;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.UserDtlsRepo;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductOrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

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

	@Autowired
	private ProductOrderService orderService;

	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private UserDtlsRepo userRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/")
	public String index() {
		return "/admin/index";
	}

	@ModelAttribute
	public void getUserDeatils(Principal p, Model m) {

		if (!ObjectUtils.isEmpty(p)) {
			String email = p.getName();
			UserDtls user = userService.getUserByEmail(email);
			m.addAttribute("user", user);
			m.addAttribute("countCart", cartService.getCountcart(user.getId()));
			;

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

	/*
	 * @GetMapping("/category") public String category(Model m) {
	 * m.addAttribute("categorys", categoryService.getAllCategory()); return
	 * "admin/category"; }
	 */
	
	@GetMapping("/category")
	public String categories(@RequestParam(name="pageNo",defaultValue = "0")Integer pageNo,@RequestParam(name="pageSize",defaultValue = "2")Integer pageSize,Model m) {
		Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
		
		
		m.addAttribute("categorys", page.getContent());
		m.addAttribute("totalCategory", page.getTotalElements());
		
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", page.getSize());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		
		return "/admin/category";
	}

	@PostMapping("/saveCategory")
	public String addCategory(@RequestParam boolean isActive,@RequestParam String name, @RequestParam MultipartFile file, HttpSession session)
			throws Exception {
		System.out.println(isActive);
		System.out.println(name);
		Category c = new Category();
		c.setIsActive(isActive);
		c.setName(name);
		
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
		return "/admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute CategoryBinding c, @RequestParam MultipartFile file, HttpSession session)
			throws IOException {
		
		Category oldCategory = categoryService.getCategoryById(c.getId());
		String imageName = file.isEmpty() ? c.getImageName() : file.getOriginalFilename();
		if (!ObjectUtils.isEmpty(oldCategory)) {
			oldCategory.setName(c.getName());
			oldCategory.setIsActive(c.isActive());
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
	public String loadViewProduct(Model m,@RequestParam(defaultValue =" ") String ch,@RequestParam(name="pageNo",defaultValue = "0") Integer pageNo,@RequestParam(name="pageSize",defaultValue = "9") Integer pageSize) {
		//List<Product> searchProduct =null;
		Page<Product> page=null;
		if(ch.equals(" ")) {
			page=productService.getAllProduct(pageNo,pageSize);
		}else {
			page = productService.searchProductPagination(ch,pageNo,pageSize);
		}
		
		
		m.addAttribute("products", page.getContent());
		m.addAttribute("pageNo",page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
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
	public String getAllUser(Model m,@RequestParam(name="type") Integer type) {
		List<UserDtls> users=null;
		if(type==1) {
			users	 = userService.getUsers("ROLE_USER");
		}else if(type==2) {
			users=userService.getUsers("ROLE_ADMIN");
		}
		m.addAttribute("userType", type);
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam String status,@RequestParam int id,String type, HttpSession session) {
		System.out.println(status);
		boolean userStatus = Boolean.parseBoolean(status);
		Boolean statusChange = userService.updateAccountStatus(id, userStatus);
		if (statusChange) {
			session.setAttribute("successMsg", "status updated sucesstfully");
		} else {
			session.setAttribute("errorMsg", "status updated failed");
		}
		return "redirect:/admin/users?type="+type;
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m,@RequestParam(name="pageNo",defaultValue = "0")Integer pageNo,@RequestParam(name="pageSize",defaultValue = "4") Integer pageSize) {
		
		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo,pageSize);
		List<ProductOrder> collect = page.getContent().parallelStream().sorted(Comparator.comparing(ProductOrder::getOrderDate).reversed()).collect(Collectors.toList());
		m.addAttribute("orders", collect);
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("totalElement", page.getTotalElements());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "/admin/order";
	}

	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {
		OrderStatus[] values = OrderStatus.values();
		// System.out.println(OrderStatus.CANCLE.getName());
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
			session.setAttribute("successMsg", "Status updated");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/orders";
	}

	@PostMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session) {

		ProductOrder order = orderService.getOrdersByOrderId(orderId);
		if (orderId.equals("")) {
			m.addAttribute("orders", orderService.getAllOrders());
		} else {

			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect Order Id");

			} else {
				m.addAttribute("orders", order);

			}
		}

		return "/admin/order";
	}
	
	@PostMapping("/search")
	public String searchProduct(@RequestParam String ch,Model m) {
		List<Product> searchProduct = productService.searchProduct(ch);
		m.addAttribute("products",searchProduct);
		return "/admin/products";
	}
	
	@GetMapping("/add-admin")
	public String loadAdminPage() {
		
		return "/admin/add_admin";
	}
	
	@PostMapping("/save-admin")
	public String saverUser(@ModelAttribute UserDtls user, HttpSession session,
			@RequestParam("img") MultipartFile file) {

		boolean checkUser = userService.checkUser(user.getEmail());
		if (!checkUser) {
			String imgName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
			user.setProfileImage(imgName);
			user.setRole("ROLE_ADMIN");
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
				session.setAttribute("successMsg", "Admin Added Successfully");
			} else {
				session.setAttribute("errorMsg", "Adding admin Failed");

			}

		} else {
			session.setAttribute("errorMsg", "Email Already exist");
		}

		return "redirect:/admin/add-admin";
	}
	
	@GetMapping("/profile")
	public String profile() {

		return "/admin/profile";
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
		return "redirect:/admin/profile";
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
			return "redirect:/admin/profile";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {

		String email = p.getName();

		UserDtls user = userRepo.findByEmail(email);

		return user;
	}

}

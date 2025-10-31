package com.ecom.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;

public interface ProductService {
 
	public Product saveProduct(Product product);
	
	public List<Product> getAllProduct();
	
	public boolean deleteProductById(int id);
	
	public Product editProductById(int id);
	
	public Product updateProduct  (Product product,MultipartFile fife);
	
	public List<Product> getAllActiveProducts();
	
}

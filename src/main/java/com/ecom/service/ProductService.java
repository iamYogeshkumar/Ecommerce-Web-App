package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;

public interface ProductService {
 
	public Product saveProduct(Product product);
	
	public Page<Product> getAllProduct(Integer pageNo,Integer pageSize);
	
	public boolean deleteProductById(int id);
	
	public Product editProductById(int id);
	
	public Product updateProduct  (Product product,MultipartFile fife);
	
	public List<Product> getAllActiveProducts();
	
	public List<Product> searchProduct(String ch);
	
	public Page<Product>  getAllActiveProductPagination(int pageNo,Integer pageSize,String category);
	
	Page<Product> searchProductPagination(String ch,Integer pageNo,Integer pageSize);

	public Page<Product> searchAllActiveProductPagination(Integer pageNo, Integer pageSize, String category,String ch);
	
}

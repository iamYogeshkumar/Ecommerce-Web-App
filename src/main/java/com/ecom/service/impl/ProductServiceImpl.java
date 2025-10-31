package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;
import com.ecom.repository.ProductRepo;
import com.ecom.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	ProductRepo repo;

	@Override
	public Product saveProduct(Product product) {

		return repo.save(product);
	}

	@Override
	public List<Product> getAllProduct() {

		return repo.findAll();
	}

	@Override
	public boolean deleteProductById(int id) {
		Product product = repo.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(product)) {
			repo.delete(product);
			return true;
		}

		return false;
	}

	@Override
	public Product editProductById(int id) {
		Product product = repo.findById(id).orElse(null);
		if (!ObjectUtils.isEmpty(product)) {
			return product;
		}
		return null;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile image) {
		Product oldProduct = editProductById(product.getId());
		String imageName = image.isEmpty() ? oldProduct.getImage() : image.getOriginalFilename();
		oldProduct.setImage(imageName);
		oldProduct.setCategory(product.getCategory());
		oldProduct.setDescription(product.getDescription());
		oldProduct.setPrice(product.getPrice());
		oldProduct.setStock(product.getStock());
		oldProduct.setTitle(product.getTitle());
		oldProduct.setIsActive(product.getIsActive());

		oldProduct.setDiscount(product.getDiscount());

		Double discount = product.getPrice() * (product.getDiscount() / 100.0);
		Double discountPrice = product.getPrice() - discount;
		oldProduct.setDiscountPrice(discountPrice);
		
		Product save = repo.save(oldProduct);
		if (!ObjectUtils.isEmpty(save)) {
			if (!image.isEmpty()) {
				try {
					File saveFile = new ClassPathResource("/static/img").getFile();
					Path path = Paths.get(
							saveFile.getAbsolutePath() + File.separator + "product_img/" + image.getOriginalFilename());
					Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return save;
		}
		return null;
	}

	@Override
	public List<Product> getAllActiveProducts() {
		List<Product> products = repo.findByIsActiveTrue();
		if(!ObjectUtils.isEmpty(products)) {
			return products;
		}
		return null;
	}

}

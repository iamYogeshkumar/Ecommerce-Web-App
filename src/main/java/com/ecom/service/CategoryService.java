package com.ecom.service;

import java.util.List;

import com.ecom.model.Category;

public interface CategoryService {

	public Category saveCategory(Category c);
	
	public List<Category> getAllCategory();
	
	public boolean existCategory(String name);
	
	public Boolean deleteCategory(int id);
	
	public Category getCategoryById(int id);
	
	public List<Category> getAllActiveCategory();
}

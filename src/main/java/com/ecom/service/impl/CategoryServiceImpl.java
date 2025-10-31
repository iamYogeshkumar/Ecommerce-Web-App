package com.ecom.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Category;
import com.ecom.repository.CategoryRepo;
import com.ecom.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepo repo;

	@Override
	public Category saveCategory(Category c) {
		Category save = repo.save(c);
		if(save!=null) {
			return save;
		}
		return null;
	}

	@Override
	public List<Category> getAllCategory() {
		return repo.findAll();
	}

	@Override
	public boolean existCategory(String name) {
		boolean f = repo.existsByName(name);
		return f;
	}

	@Override
	public Boolean deleteCategory(int id) {
		 Category category = repo.findById(id).orElse(null);
		 if(!ObjectUtils.isEmpty(category)) {
			 repo.delete(category);
			 return true;
		 }
		return false;
	}

	@Override
	public Category getCategoryById(int id) {
		Category category = repo.findById(id).orElse(null);
		return category;
	}

	@Override
	public List<Category> getAllActiveCategory() {
		List<Category> categories = repo.findByisActiveTrue();
		return categories;
	}

}

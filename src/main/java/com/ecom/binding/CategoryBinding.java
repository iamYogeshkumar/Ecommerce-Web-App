package com.ecom.binding;

public class CategoryBinding {
	private Integer id;
	private String name;
	private String imageName;
	private boolean isActive;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	@Override
	public String toString() {
		return "CategoryBinding [id=" + id + ", name=" + name + ", imageName=" + imageName + ", isActive=" + isActive
				+ "]";
	}
	
	
}

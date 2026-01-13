package com.ecom.util;

public enum OrderStatus {

	IN_PROGRESS(1,"In Progress"),
	ORDER_RECEIVED(2,"Order received"),
	PRODUCT_PACKED(3,"Product packed"),
	OUT_FOR_DELIVERY(4,"Out for delivery"),
	DELIVERED(5,"Delivered"),CANCLE(6,"Cancelled"),SUCCESS(7,"Order Success");
	
    private Integer id;
	
	private String names;
	
	private OrderStatus(Integer id, String name) {
		this.id = id;
		this.names = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return names;
	}

	public void setName(String name) {
		this.names = name;
	}
	
	

	
	
	
}

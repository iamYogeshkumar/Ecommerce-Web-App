package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;

public interface ProductOrderService {

	public void saveOrder(Integer userId,OrderRequest orderRequest);
	
	public List<ProductOrder> getOrdersByUser(Integer userId);
	
	public ProductOrder updateOrderStatus(Integer id,String status);
	
	List<ProductOrder> getAllOrders();
	
	ProductOrder getOrdersByOrderId(String orderId);
	
	Page<ProductOrder> getAllOrdersPagination(Integer pageNo,Integer pageSize);
	
}

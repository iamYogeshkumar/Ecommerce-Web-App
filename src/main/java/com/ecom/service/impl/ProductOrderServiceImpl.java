package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.service.ProductOrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

@Service
public class ProductOrderServiceImpl implements ProductOrderService {

	@Autowired
	private ProductOrderRepository orderRepository;
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Override
	public void saveOrder(Integer userId,OrderRequest orderRequest) {
		List<Cart> carts = cartRepository.findByUserId(userId);
		List<ProductOrder> po=new ArrayList<>();
		
		for(Cart c:carts) {
			
			Product product = c.getProduct();
			Double discountPrice = product.getDiscountPrice();
			Integer quantity = c.getQuantity();
			Double totalPrice = quantity*discountPrice;
			UserDtls user = c.getUser();
			
		    OrderAddress orderAddress = new OrderAddress();
			BeanUtils.copyProperties(orderRequest, orderAddress);
			
			ProductOrder order=new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString().substring(0,8));
			order.setOrderDate(LocalDate.now());
			order.setProduct(product);
			order.setPrice(totalPrice);
			order.setQuantity(quantity);
			order.setUser(user);
			order.setOrderStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());
			order.setAddress(orderAddress);
			po.add(order);
;		}
		
		List<ProductOrder> saveAll = orderRepository.saveAll(po);
		commonUtil.sendMAilForProductOrderByuser(saveAll);
		removeItemFromCartAfterOrder(userId);
		
	}
	
	private void removeItemFromCartAfterOrder(int userid) {
		List<Cart> carts = cartRepository.findByUserId(userid);
		cartRepository.deleteAll(carts);
	}

	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
		List<ProductOrder> byUserId = orderRepository.findByUserId(userId);
		List<ProductOrder> collect = byUserId.stream()
			    .sorted(Comparator.comparing(ProductOrder::getOrderDate).reversed())
			    .collect(Collectors.toList());


		return collect;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id,String status) {
		 Optional<ProductOrder> byId = orderRepository.findById(id);
		 if(byId.isPresent()) {
			 ProductOrder productOrder = byId.get();
			 productOrder.setOrderStatus(status);
			 ProductOrder save = orderRepository.save(productOrder);
			 return save;
		 }
		
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		List<ProductOrder> all = orderRepository.findAll();
		List<ProductOrder> collect = all.parallelStream().sorted(Comparator.comparing(ProductOrder::getOrderDate).reversed()).collect(Collectors.toList());
		return collect;
	}
	
	

	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		PageRequest of = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(of);
	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
		 
	}

}

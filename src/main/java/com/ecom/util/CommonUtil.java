package com.ecom.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;

	public boolean sendMail(String email, String url, UserDtls user) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom("ys4139887@gmail.com", "Shoping Cart");
			helper.setTo(email);
			helper.setSubject("Reset Your Password");

			String body = readEmailTemplate(user, url);

			helper.setText(body, true);
			mailSender.send(mimeMessage);

			return true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private String readEmailTemplate(UserDtls user, String url) throws Exception {
		StringBuilder sb = new StringBuilder();
		String line = null;
		String content = "";
		FileReader fileReader = new FileReader(AppConstant.FILE_NAME);
		BufferedReader br = new BufferedReader(fileReader);
		while ((line = br.readLine()) != null) {
			sb.append(line).append(System.lineSeparator());
		}
		content = sb.toString();
		content = content.replace("{FULLNAME}", user.getName());
		content = content.replace("{LINK}", url); // vdNo:19 43:49
		System.out.println(content);
		br.close();
		return content;

	}

	public static String generateUrl(HttpServletRequest request) {
		String requestURL = request.getRequestURL().toString(); // http://localhost:8080/forget-password

		// String requestURI = request.getRequestURI(); /forget-password
		System.out.println("contextPath " + request.getRequestURI());
		requestURL = requestURL.replace(request.getServletPath(), "");
		System.out.println(requestURL);
		return requestURL;
	}
	/*
	 * request.getRequestURL().toString(); http://localhost:8080/forget-password
	 * request.getServletPath(); // /forget-password
	 * 
	 * request.getServletPath(); /forget-password
	 * 
	 * 
	 */

	public Boolean sendMAilForProductOrder(ProductOrder order) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom("ys4139887@gmail.com", "Shoping Cart");
			helper.setTo(order.getAddress().getEmail());
			helper.setSubject("Order status");

			String body = readOrderStatusTxt(order);
             
			helper.setText(body, true);
			mailSender.send(mimeMessage);

			return true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}
	
	public Boolean sendMAilForProductOrderByuser(List<ProductOrder> order) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom("ys4139887@gmail.com", "Shoping Cart");
			helper.setTo(order.get(0).getAddress().getEmail());
			helper.setSubject("Order status");
            
			StringBuilder sb=new StringBuilder();
			for(ProductOrder o:order) {
				String body = readOrderStatusTxt(o);
				sb.append(body).append("<br><br>");
			}
			
             
			helper.setText(sb.toString(), true);
			mailSender.send(mimeMessage);

			return true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}
	
	private String readOrderStatusTxt(ProductOrder order) {
	String msg=	"Hello {userName}, Thank you your Order <b>{Placed Successful}</b>. <br><br>" +
		"Product Details: {productname} <br>" +
	"	 Name   :   {name}  <br><br>"+
	"	 Category : {category}  <br>"+
	"   Quantity : {quantity}  <br> "+
	"	Price    : {price}      <br> "+
	"	Payment  : {paymentType} <br> "+
	"   Thank you" ;
	
	
	
	
	msg=msg.replace("{userName}", order.getAddress().getFirstName());	
	msg=msg.replace("{Placed Successful}", order.getOrderStatus());
	msg=msg.replace("{productname}", order.getProduct().getDescription());
	msg=msg.replace("{name}", order.getProduct().getTitle());
	msg=msg.replace("{category}", order.getProduct().getCategory());
	
	msg=msg.replace("{quantity}", order.getQuantity().toString());
	
	msg=msg.replace("{price}", order.getPrice().toString());
	msg=msg.replace("{paymentType}", order.getPaymentType());
		
	return msg;
	}
}

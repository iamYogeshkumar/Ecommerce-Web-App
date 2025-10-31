package com.ecom.util;

import java.io.BufferedReader;
import java.io.FileReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	@Autowired
	private  JavaMailSender mailSender;

	@SuppressWarnings("resource")
	public  boolean sendMail(String email, String url, UserDtls user) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		
		
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom("ys4139887@gmail.com", "Shoping Cart");
			helper.setTo(email);
			helper.setSubject("Reset Your Password");

			String body=readEmailTemplate(user,url);

			helper.setText(body, true);
			mailSender.send(mimeMessage);

			return true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private String readEmailTemplate(UserDtls user,String url) throws Exception {
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
}

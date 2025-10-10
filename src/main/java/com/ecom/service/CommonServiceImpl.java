package com.ecom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	HttpSession session;
	
	@Override
	public void removeSession() {
		session.removeAttribute("successMsg");
		session.removeAttribute("errorMsg");
	}

}

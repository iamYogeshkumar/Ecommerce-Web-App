package com.ecom.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserDtlsRepo;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserDtlsRepo userRepo;

	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
	        AuthenticationException exception) throws IOException, ServletException {
	    
	    String email = request.getParameter("username");
	    UserDtls user = userRepo.findByEmail(email);

	    if (user != null) {
	        System.out.println(user.getIsEnable());

	        if (user.getIsEnable()) {
	            if (user.getAccountNonLocked()) {
	                Integer failedAttempt = user.getFailedAttempt();
	                if (failedAttempt < AppConstant.ATTEMPT_TIME - 1) {
	                    userService.increaseFailedattempt(user);
	                } else {
	                    userService.userAccountLocked(user);
	                    exception = new LockedException("Your account is locked! Too many failed attempts.");
	                }
	            } else {
	                if (userService.unlockAccountTimeExpired(user)) {
	                    exception = new LockedException("Your account is unlocked! Please try to login again.");
	                } else {
	                    exception = new LockedException("Your account is locked! Please try again later.");
	                }
	            }
	        } else {
	            exception = new LockedException("Your account is inactive.");
	        }

	    } else {
	        // No user found
	        exception = new LockedException("Wrong email or password.");
	    }

	    super.setDefaultFailureUrl("/signin?error");
	    super.onAuthenticationFailure(request, response, exception);
	}

}

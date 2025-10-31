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
		System.out.println(user.getIsEnable());
		if (user.getIsEnable()) {
			if (user.getAccountNonLocked()) {
				Integer failedAttempt = user.getFailedAttempt();
				if (failedAttempt < AppConstant.ATTEMPT_TIME) {
					userService.increaseFailedattempt(user);
				} else {
					userService.userAccountLocked(user);
					exception = new LockedException("your account is Locked !! failed attempt 3");

				}
			} else {

				if (userService.unlockAccountTimeExpired(user)) {
					exception = new LockedException("your account is unlocked ! please try to login");
				} else {
					exception = new LockedException("your account is Locked !! please try after some time");

				}

			}
		} else {
			exception = new LockedException("your account is inactive");
		}
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

}

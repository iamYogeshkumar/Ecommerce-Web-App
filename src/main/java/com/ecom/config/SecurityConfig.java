package com.ecom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	@Lazy
	private AuthenticationFailureHandler authenticationFailureHandler;
	
	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;

	@Autowired
	private UserDetailsService userDetailsService;

	@Bean
	public PasswordEncoder pwdEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {

		DaoAuthenticationProvider dao = new DaoAuthenticationProvider(userDetailsService);
		dao.setPasswordEncoder(pwdEncoder());
		return dao;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     http.csrf(csrf->csrf.disable()).cors(cors->cors.disable())
     .authorizeHttpRequests(req->req.requestMatchers("/user/**").hasRole("USER")
    		                           . requestMatchers("/admin/**").hasRole("ADMIN")
    		                           .requestMatchers("/**").permitAll()   )
    		                           .formLogin(form->form.loginPage("/signin")
    		                        		                .loginProcessingUrl("/login")
    		                        	//	                .defaultSuccessUrl("/")
    		                        		                .failureHandler(authenticationFailureHandler)
    		                        		                .successHandler(authenticationSuccessHandler)
    		                        		     )
    		                           .logout(logout->logout.permitAll());
		
		return http.build();
	}
}

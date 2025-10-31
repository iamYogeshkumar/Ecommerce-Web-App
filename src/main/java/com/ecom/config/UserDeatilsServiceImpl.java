package com.ecom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserDtlsRepo;

@Service
public class UserDeatilsServiceImpl implements UserDetailsService {

	@Autowired
	private UserDtlsRepo userRepo;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserDtls user = userRepo.findByEmail(email);
		if(ObjectUtils.isEmpty(user)) {
			throw new UsernameNotFoundException("User not found");
		}
		
		return new CustomUser(user);
	}

}

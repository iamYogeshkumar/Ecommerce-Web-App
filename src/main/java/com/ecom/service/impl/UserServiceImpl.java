package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserDtlsRepo;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDtlsRepo userRepo;

	@Autowired
	private PasswordEncoder encoder;

	@Override
	public UserDtls saveUser(UserDtls user) {
		user.setPassword(encoder.encode(user.getPassword()));
		if(user.getRole()==null || user.getRole().equals("")) {
			user.setRole("ROLE_USER");
		}
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		return userRepo.save(user);
	}

	@Override
	public boolean checkUser(String email) {
		UserDtls user = userRepo.findByEmail(email);
		if (!ObjectUtils.isEmpty(user)) {
			return true;
		}
		return false;
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		return userRepo.findByEmail(email);

	}

	@Override
	public List<UserDtls> getUsers(String role) {
		List<UserDtls> users = userRepo.findByRole(role);
		return users;
	}

	@Override
	public boolean updateAccountStatus(int id, boolean status) {
		UserDtls user = userRepo.findById(id).orElse(null);
		user.setIsEnable(status);
		userRepo.save(user);
		return true;

	}

	@Override
	public void increaseFailedattempt(UserDtls user) {
		int attempt = user.getFailedAttempt() + 1;
		user.setFailedAttempt(attempt);
		userRepo.save(user);

	}

	@Override
	public void userAccountLocked(UserDtls user) {
         user.setAccountNonLocked(false);//acc is locked
         user.setLocktime(new Date());
         userRepo.save(user);
	}

	@Override
	public boolean unlockAccountTimeExpired(UserDtls user) {
		long locktime = user.getLocktime().getTime();  //6pm
		long unlockTime =locktime+AppConstant.UNLOCK_DURATION_TIME; //7pm
		
		long currentTime=System.currentTimeMillis();
		if(unlockTime<currentTime) {
			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLocktime(null);
			userRepo.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void restAttempt(int userId) {
		
	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {
		UserDtls user = userRepo.findByEmail(email);
		user.setResetToken(resetToken);
		userRepo.save(user);
		
	}

	@Override
	public UserDtls getUserByToken(String token) {
		return userRepo.findByResetToken(token);
	}

	@Override
	public UserDtls updateUserPwd(String token, String password) {
		UserDtls user = userRepo.findByResetToken(token);
		user.setResetToken(null);
		user.setPassword(encoder.encode(password));
		return userRepo.save(user);
		 
	}

	@Override
	public UserDtls updateUserProfile(UserDtls user,MultipartFile img) throws Exception {
		UserDtls dbUser = userRepo.findById(user.getId()).get();
		if(!img.isEmpty()) {
			dbUser.setProfileImage(img.getOriginalFilename());
		}
		if(!ObjectUtils.isEmpty(dbUser)) {
			dbUser.setName(user.getName());
			dbUser.setMobileNUmber(user.getMobileNUmber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			dbUser=userRepo.save(dbUser);
		}
		
		if(!img.isEmpty()) {
			File saveFile=new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.separator+img.getOriginalFilename());
			Files.copy(img.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
		}
		
		
		
		return dbUser;
	}

	@Override
	public UserDtls updateUser(UserDtls user) {
		
		return userRepo.save(user);
	}

}

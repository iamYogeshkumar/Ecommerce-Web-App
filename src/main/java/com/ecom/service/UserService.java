package com.ecom.service;

import java.util.List;

import com.ecom.model.UserDtls;

public interface UserService {

	public UserDtls saveUser(UserDtls user);

	public boolean checkUser(String email);

	public UserDtls getUserByEmail(String email);

	public List<UserDtls> getUsers(String role);

	public boolean updateAccountStatus(int id,boolean status);
	
	public void increaseFailedattempt(UserDtls user);
	
	public void userAccountLocked(UserDtls user);
	
	public boolean unlockAccountTimeExpired(UserDtls user);
	
	public void restAttempt(int userId);

	public void updateUserResetToken(String email, String resetToken);
	
	public UserDtls getUserByToken(String token);
	
	public UserDtls updateUserPwd(String token,String password);
}

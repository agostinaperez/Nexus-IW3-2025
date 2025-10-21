package edu.iua.nexus.auth.model;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import edu.iua.nexus.auth.BadPasswordException;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.NotFoundException;

public interface IUserAuthBusiness {
	public User load(String usernameOrEmail) throws NotFoundException, BusinessException;

	public void changePassword(String usernameOrEmail, String oldPassword, String newPassword, PasswordEncoder pEncoder)
			throws BadPasswordException, NotFoundException, BusinessException;

	public void disable(String usernameOrEmail) throws NotFoundException, BusinessException;

	public void enable(String usernameOrEmail) throws NotFoundException, BusinessException;
	
	public List<User> list() throws BusinessException;

}
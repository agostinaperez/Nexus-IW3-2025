package edu.iua.nexus.model.business.interfaces;

import edu.iua.nexus.auth.model.User;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;

import java.util.List;

public interface IUserBusiness {

    public List<User> list() throws BusinessException;

    public User load(long id) throws NotFoundException, BusinessException;

    public User load(String user) throws NotFoundException, BusinessException;

    public User add(User user) throws FoundException, BusinessException;

    public User update(User user) throws NotFoundException, BusinessException, FoundException;

    public void delete(User user) throws NotFoundException, BusinessException;

    public void delete(long id) throws NotFoundException, BusinessException;

}

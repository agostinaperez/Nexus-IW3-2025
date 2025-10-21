package edu.iua.nexus.model.business.impl;

import edu.iua.nexus.auth.model.User;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.model.business.interfaces.IUserBusiness;
import edu.iua.nexus.model.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class UserBusiness implements IUserBusiness {

    @Autowired
    private UserRepository userDAO;

    @Override
    public List<User> list() throws BusinessException {
        try {
            return userDAO.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public User load(long id) throws NotFoundException, BusinessException {
        Optional<User> userFound;

        try {
            userFound = userDAO.findById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (userFound.isEmpty())
            throw NotFoundException.builder().message("No se encuentra el Usuario id= " + id).build();
        return userFound.get();
    }

    @Override
    public User load(String user) throws NotFoundException, BusinessException {
        Optional<User> userFound;

        try {
            userFound = userDAO.findByUsername(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (userFound.isEmpty())
            throw NotFoundException.builder().message("No se encuentra el Usuario " + user).build();
        return userFound.get();
    }

    @Override
    public User add(User user) throws FoundException, BusinessException {

        try {
            load(user.getIdUser());
            throw FoundException.builder().message("Se encontro el Usuario con id = " + user.getIdUser()).build();
        } catch (Exception ignored) {
        }

        try {
            load(user.getUsername());
            throw FoundException.builder().message("Se encontro el Usuario " + user.getUsername()).build();
        } catch (NotFoundException ignored) {
        }

        try {
            return userDAO.save(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }


    @Override
    public User update(User user) throws NotFoundException, BusinessException, FoundException {

        User userToUpdate = load(user.getIdUser());
        Optional<User> userFound;

        try {
            userFound = userDAO.findByUsernameAndIdUserNot(user.getUsername(), user.getIdUser());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

        if (userFound.isPresent()) {
            throw FoundException.builder().message("Se encontró el Usuario " + user.getUsername()).build();
        }

        try {
            user.setPassword(userToUpdate.getPassword());
            return userDAO.save(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public void delete(User user) throws NotFoundException, BusinessException {
        delete(user.getIdUser());
    }

    @Override
    public void delete(long id) throws NotFoundException, BusinessException {
        load(id);

        try {
            userDAO.deleteById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

}
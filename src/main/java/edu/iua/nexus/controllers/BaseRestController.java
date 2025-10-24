package edu.iua.nexus.controllers;

import org.springframework.security.core.context.SecurityContextHolder;

import edu.iua.nexus.auth.model.User;

import org.springframework.security.core.Authentication;

//extiendo todos los controladores desde acá
public class BaseRestController {
    protected User getUserLogged() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        return user;
    }
}

package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.User;

public interface UserService {

    User create(User user);

    User findByEmail(String email);
}
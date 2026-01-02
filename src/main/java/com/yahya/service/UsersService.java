package com.yahya.service;

import com.yahya.model.Users;

import java.util.List;


public interface UsersService {
    void saveUsers(Users users);
    List<Users> getAllUsers();
    Users findByEmailAndPassword(String email, String password);
    Users findByEmail(String email);
    Users findById(String id);
}

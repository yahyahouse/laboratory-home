package com.yahya.service;

import com.yahya.model.Users;
import com.yahya.repository.UsersRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersRepository usersRepository;

    @PostConstruct
    public void init() {
        if (usersRepository.count() == 0) {
            Users user = new Users();
            user.setId("1");
            user.setName("admin");
            user.setEmail("admin");
            user.setPassword("admin");
            usersRepository.save(user);
        }
    }
    @Override
    public void saveUsers(Users users) {
        Optional<Users> user = usersRepository.findByEmail(users.getEmail());
        if (user.isPresent()) {
            throw new RuntimeException("Email already exists");
        }else {
            users.setId(UUID.randomUUID().toString());
            usersRepository.save(users);
        }

    }

    @Override
    public List<Users> getAllUsers() {
        return usersRepository.getAllUsers();
    }

    @Override
    public Users findByEmailAndPassword(String email, String password) {
        return usersRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public Users findByEmail(String email) {
        return usersRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Users findById(String id) {
        return usersRepository.findById(id).orElse(null);
    }
}

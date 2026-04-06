package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.UserListItemResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserListItemResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(UserListItemResponse::new)
                .collect(Collectors.toList());
    }
}

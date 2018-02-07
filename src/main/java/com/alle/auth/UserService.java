package com.alle.auth;

import java.util.List;

public interface UserService {
    User findUserByEmail(String email);

    void saveUser(User user);

    String findCurrentUserId();

    User findUser(Integer id);

    List<User> allUser();

}
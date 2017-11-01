package com.alle.auth;

public interface UserService {
     User findUserByEmail(String email);
	 void saveUser(User user);
}
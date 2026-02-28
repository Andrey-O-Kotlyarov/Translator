package testgroup.service;

import java.util.Optional;
import testgroup.model.User;

public interface UserService {
    public Long createUser(String mail, String username, String pass); 
    public Optional<User> getUserById(Long id); 
    public Optional<User> getUserByUsernameAndPass(String username, String pass); 

}

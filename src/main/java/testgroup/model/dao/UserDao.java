package testgroup.model.dao;

import java.util.Optional; 
import testgroup.model.entity.User;

public interface UserDao {
    public Long createUser(String mail, String username, String pass); 
    public Optional<User> getUserById(Long id); 
    public Optional<User> getUserByUsernameAndPass(String username, String pass); 
    public Optional<User> getUserByUsername(String username); 

}

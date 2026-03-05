package testgroup.service;

import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import testgroup.dao.UserRepository;
import testgroup.model.User;

@Service
public class UserServiceImpl implements UserService { 

    @Autowired
    private UserRepository userRepository;

    @Override
    public Long createUser(String mail, String username, String pass) {
        User user = new User();
        user.setMail(mail);
        user.setUsername(username);
        user.setPassword(pass); 
        
        User savedUser = userRepository.save(user);
        Long id = savedUser.getId(); 
        return id; 
    }

    @Override
    public Optional<User> getUserById(Long id) { 
        @SuppressWarnings("null")
        Optional<User> op = userRepository.findById(id); 
        return op;         
    }


    @Override
    public Optional<User> getUserByUsernameAndPass(String username, String pass) {         
        Optional<User> op = userRepository.findByUsernameAndPassword(username, pass); 
        return op; 
    } 


    @Override
    public Optional<User> getUserByUsername(String username) {
        Optional<User> op = userRepository.findByUsername(username); 
        return op;
    }

} 
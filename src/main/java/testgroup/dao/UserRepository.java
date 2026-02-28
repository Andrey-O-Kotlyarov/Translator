package testgroup.dao; 

import java.util.Optional; 
import org.springframework.data.jpa.repository.JpaRepository; 
import testgroup.model.User;

public interface UserRepository extends JpaRepository<User, Long> { 
    Optional<User> findByUsernameAndPassword(String username, String password);

}

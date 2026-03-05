package testgroup.dao;

import java.util.Optional; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 
import testgroup.model.Word;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> { 
    Optional<Word> findByRusWord(String rusWord); 

}

package testgroup.dao;

import java.util.List;
import java.util.Optional; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 
import testgroup.model.User;
import testgroup.model.Word;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> { 
    Optional<Word> findByRusWord(String rusWord); 
    Optional<Word> findByRusWordAndUser(String rusWord, User user); 

    // Метод для получения всех слов по пользователю
    List<Word> findAllByUser(User user); 

    // Метод для получения всех слов по id пользователя
    List<Word> findAllByUser_Id(Long userId); 

} 
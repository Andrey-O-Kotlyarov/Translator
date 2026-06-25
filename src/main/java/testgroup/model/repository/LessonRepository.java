package testgroup.model.repository;

import java.util.List;
import java.util.Optional; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 
import testgroup.model.entity.Lesson;
import testgroup.model.entity.User;

@Repository
public interface LessonRepository extends JpaRepository<testgroup.model.entity.Lesson, Long> { 
    
    // метод возвращает урок с наибольшим значением Number для заданного пользователя
    Optional<Lesson> findTopByUserOrderByNumberDesc(User user);

    // метод возвращает урок с наибольшим значением Number для заданного userId
    Optional<Lesson> findTopByUser_IdOrderByNumberDesc(Long userId); 

    // Метод для получения всех уроков по пользователю
    List<Lesson> findAllByUser(User user);

    // Метод для получения всех уроков по id пользователя
    List<Lesson> findAllByUser_Id(Long userId); 
} 

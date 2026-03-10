package testgroup.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 
import testgroup.model.Lesson;
import testgroup.model.User;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> { 
    
    // метод возвращает урок с наибольшим значением Number для заданного пользователя
    Optional<Lesson> findTopByUserOrderByNumberDesc(User user);

    // метод возвращает урок с наибольшим id для заданного userId
    Optional<Lesson> findTopByUser_IdOrderByNumberDesc(Long userId);

}

package testgroup.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 
import testgroup.model.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

}

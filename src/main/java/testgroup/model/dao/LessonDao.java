package testgroup.model.dao;

import java.util.List;
import java.util.Optional;
import testgroup.model.entity.Lesson;
import testgroup.model.entity.User;
import testgroup.service.lessonformatter.LessonUnit;

public interface LessonDao { 

    public Long createLesson(Long number, String title, List<LessonUnit> content, User user); 
    public Optional<Lesson> getLatestLessonForUser(User user); 
    public Optional<Lesson> getLatestLessonForUserId(Long userId); 
    public Optional<Lesson> getLessonById(Long id); 
    public List<Lesson> getAllLessonsForUser(User user); 
    public List<Lesson> getAllLessonsForUserId(Long userId); 
    public void deleteLesson(Long id); 

}

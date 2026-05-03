package testgroup.datawork.service;

import java.util.List;
import java.util.Optional; 
import testgroup.datawork.entity.Lesson;
import testgroup.datawork.entity.User;

public interface LessonService { 

    public Long createLesson(Long number, String title, String content, User user); 
    public Optional<Lesson> getLatestLessonForUser(User user); 
    public Optional<Lesson> getLatestLessonForUserId(Long userId); 
    public List<Lesson> getAllLessonsForUser(User user); 
    public List<Lesson> getAllLessonsForUserId(Long userId); 
    public void deleteLesson(Long id); 

}

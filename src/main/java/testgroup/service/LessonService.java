package testgroup.service;

import java.util.Optional; 
import testgroup.model.Lesson;
import testgroup.model.User;

public interface LessonService { 

    public Long createLesson(Long number, String title, String content, User user); 
    public Optional<Lesson> getLatestLessonForUser(User user); 
    public Optional<Lesson> getLatestLessonForUserId(Long userId); 
}

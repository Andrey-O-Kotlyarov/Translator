package testgroup.service;

import java.util.List;
import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import testgroup.dao.LessonRepository;
import testgroup.model.Lesson;
import testgroup.model.User;

@Service
public class LessonServiceImpl implements LessonService { 

    @Autowired
    private LessonRepository lessonRepository; 


    @Override
    public Long createLesson(Long number, String title, String content, User user) { 
        Lesson lesson = new Lesson(); 
        lesson.setNumber(number); 
        lesson.setTitle(title); 
        lesson.setContent(content);  
        lesson.setUser(user);        
        Lesson savedLesson = lessonRepository.save(lesson); 
        Long id = savedLesson.getId(); 
        return id; 
    }


    @Override
    public Optional<Lesson> getLatestLessonForUser(User user) {
        return lessonRepository.findTopByUserOrderByNumberDesc(user);
    }


    @Override
    public Optional<Lesson> getLatestLessonForUserId(Long userId) {
        return lessonRepository.findTopByUser_IdOrderByNumberDesc(userId);
    } 


    @Override
    public List<Lesson> getAllLessonsForUser(User user) {
        return lessonRepository.findAllByUser(user);
    }


    @Override
    public List<Lesson> getAllLessonsForUserId(Long userId) {
        return lessonRepository.findAllByUser_Id(userId);
    }


    @Override
    @SuppressWarnings("null")
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

} 
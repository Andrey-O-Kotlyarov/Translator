package testgroup.model.entity;

import java.util.List; 
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import testgroup.service.lessonformatter.LessonUnit;
import testgroup.service.lessonformatter.LessonUnitListConverter;

@Data
@Entity
@Table(name = "lessons")
public class Lesson { 

    // Идентификатор урока
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    // номер урока для данного пользователя 
    @Column(nullable = false)
    private Long number; 

    // Название урока
    @Column(nullable = false)
    private String title;

    // Основной текст урока
    @Column(columnDefinition="TEXT") 
    @Convert(converter = LessonUnitListConverter.class) 
    private List<LessonUnit> content;     

    // Связываем lesson с user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; 

} 
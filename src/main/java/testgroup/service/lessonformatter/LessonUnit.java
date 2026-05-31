package testgroup.service.lessonformatter;

import java.util.List; 
import lombok.Data;
import lombok.NonNull;

@Data
public class LessonUnit { 
    @NonNull
    private String textFragment; // Фрагмент текста 

    @NonNull
    private List<WordPair> pairs; // Список слов к этому фрагменту 
} 
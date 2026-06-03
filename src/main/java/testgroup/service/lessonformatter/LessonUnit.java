package testgroup.service.lessonformatter;

import java.util.List; 
import lombok.Data;
import lombok.NonNull;

@Data
public class LessonUnit { 
    
    @NonNull
    private List<WordPair> pairs; // Список слов с переводами к фрагменту текста
    
    @NonNull
    private String textFragment; // сам фрагмент текста 

    @NonNull
    private String translation; // его перевод
} 
package testgroup.service.lessonformatter;

import java.util.List; 
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class LessonUnit { 
    
    @NonNull
    private List<WordPair> pairs; // Список слов с переводами к фрагменту текста
    
    @NonNull
    private String textFragment; // сам фрагмент текста 

    @NonNull
    private String translation; // его перевод
} 
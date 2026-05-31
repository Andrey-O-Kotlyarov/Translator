package testgroup.service.lessonformatter;

import lombok.Data;
import lombok.NonNull;

@Data
public class WordPair { 
    @NonNull
    private String word; 

    @NonNull
    private String translation; 
} 
package testgroup.service.lessonformatter;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor 
@RequiredArgsConstructor 
public class WordPair { 
    @NonNull
    private String word; 

    @NonNull
    private String translation; 
} 
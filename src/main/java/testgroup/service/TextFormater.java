package testgroup.service;

import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import testgroup.model.User;
import testgroup.model.Word; 

@Service
public class TextFormater { 

    @Autowired
    private WordService wordService;  
    
    @Autowired
    private UserService userService;

    // временная заглушка для метода перевода слов
    public String translate(String wordForTranslating) {
        String result = wordForTranslating.replaceAll("[аеуёиоуыэяю]", "");
        return result;
    }


    // метод для составления урока 
    public String makeLesson(String originText, String nameOfCurrentUser) {         
        String fragment = ""; 
        String contextVocabulary = ""; 
        User currentUser = new User(); 
        String[] words = originText.split("[\s\r\n]+"); 

        Optional<User> userOptional = userService.getUserByUsername(nameOfCurrentUser); 
        if (userOptional.isPresent()) {
                currentUser = userOptional.get(); 
        } else { 
            System.out.println("пользователь не найден"); 
        }

        for (String word : words) { 
            word = word.replaceAll("[\\p{Punct}\\s–—]+", " ").trim(); 
            word = word.toLowerCase(); 

            Optional<Word> op = wordService.getWordByRusWord(word); 

            if (op.isPresent()) {
                fragment = fragment + word + " "; 
            } else {
                fragment = fragment + word + " "; 
                String translatedWord = translate(word); 
                try {
                    wordService.createWord(word, translatedWord, currentUser); 
                } catch (Exception e) { 
                    e.printStackTrace(); 
                    //System.out.println("какая-то проблема с сохранением слова в базу");                     
                }   
                contextVocabulary = contextVocabulary + word + "  -  " + translatedWord + "\n"; 
            } 
        } 

        String result = contextVocabulary; 
        return result; 
        
    }

}

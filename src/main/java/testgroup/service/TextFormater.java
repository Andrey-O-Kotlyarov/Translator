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
        String publication = ""; 

        String[] words = originText.split("[\s\r\n]+"); 
        Optional<User> userOptional = userService.getUserByUsername(nameOfCurrentUser); 

        // если пользователь не найден, то обращений к базе делать не будем,
        // просто по-быстрому составляем контекстный словарь и возвращаем его: 
        if (!userOptional.isPresent()) {
            System.out.println("пользователь, для которого формируется урок, в базе не найден"); 
            int counter = 0; 

            for (String word : words) { 
                String execWord = word
                    .replaceAll("[\\p{Punct}\\s–—]+", " ")
                    .trim() 
                    .toLowerCase(); 
                fragment = fragment + word + " "; 

                if (!execWord.isBlank()) {
                    String translatedWord = translate(execWord);                 
                    contextVocabulary = contextVocabulary 
                        + execWord + " " + " - " + " " + translatedWord + "\n"; 
                } else {
                    counter--; 
                }
                
                if (counter < 30) {                     
                    counter++; 
                } else { 
                    counter = 0; 
                    publication = publication 
                        + contextVocabulary + "\n" + "\n" 
                        + fragment + "\n" + "\n" 
                        + "===========================================================" 
                        + "\n" + "\n"; 
                    contextVocabulary = ""; 
                    fragment = ""; 
                }                
            }
            return publication; 
        }

        // а если пользователь найден, то будем все новые слова добавлять в базу: 
        User currentUser = userOptional.get();

        for (String word : words) { 
            word = word.replaceAll("[\\p{Punct}\\s–—]+", " ").trim(); 
            word = word.toLowerCase(); 
            
            Optional<Word> wordOp = Optional.empty(); 
            try { 
                wordOp = wordService.getWordByRusWordAndUser(word, currentUser); 
            } catch (Exception e) { 
                //e.printStackTrace(); 
                System.out.println("какая-то проблема с поиском слова в базе");                     
            }               

            if (wordOp.isPresent()) {
                fragment = fragment + word + " "; 
            } else {
                fragment = fragment + word + " "; 
                String translatedWord = translate(word); 
                try {
                    wordService.createWord(word, translatedWord, currentUser); 
                } catch (Exception e) { 
                    //e.printStackTrace(); 
                    System.out.println("какая-то проблема с сохранением слова в базу");                     
                }   
                contextVocabulary = 
                    contextVocabulary + word + " " + " - " + " " + translatedWord + "\n"; 
            } 
        } 
        
        if (contextVocabulary.isBlank()) { 
            return "все слова из переданного текста уже есть в словаре данного пользователя"; 
        }

        String result = contextVocabulary; 
        return result; 
    }

}

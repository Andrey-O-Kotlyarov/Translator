package testgroup.service;

import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import testgroup.model.Lesson;
import testgroup.model.User;
import testgroup.model.Word; 

@Service
public class TextFormater { 

    @Autowired
    private WordService wordService;  
    
    @Autowired
    private UserService userService; 

    @Autowired
    private LessonService lessonService; 

    // временная заглушка для метода перевода слов
    public String translate(String wordForTranslating) {
        String result = wordForTranslating.replaceAll("[аеуёиоуыэяю]", "");
        return result;
    }


    // метод для составления текста урока 
    public String makeLesson(String originText, String nameOfCurrentUser) {         
        String fragment = ""; 
        String contextVocabulary = ""; 
        String publication = ""; 
        String title = ""; 

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
                
                if (counter < 29) {                     
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
        int counter = 0; 
        int titleCounter = 0; 

        for (String word : words) { 
            String execWord = word
                    .replaceAll("[\\p{Punct}\\s–—]+", " ")
                    .trim() 
                    .toLowerCase();    
                    
            if (titleCounter < 8) {
                title = title + word + " "; 
                titleCounter++; 
            }
            
            Optional<Word> wordOp = Optional.empty(); 
            try { 
                wordOp = wordService.getWordByRusWordAndUser(execWord, currentUser); 
            } catch (Exception e) { 
                //e.printStackTrace(); 
                System.out.println("какая-то проблема с поиском слова в базе");                     
            }               

            if (wordOp.isPresent()) {
                fragment = fragment + word + " "; 
            } else { 
                counter++; 
                fragment = fragment + word + " "; 
                String translatedWord = translate(execWord); 
                try {
                    wordService.createWord(execWord, translatedWord, currentUser); 
                } catch (Exception e) { 
                    //e.printStackTrace(); 
                    System.out.println("какая-то проблема с сохранением слова в базу");                     
                }   
                contextVocabulary = 
                    contextVocabulary + execWord + " " + " - " + " " + translatedWord + "\n"; 
            } 

            if (counter > 29 && (
                    word.endsWith(".") || 
                    word.endsWith("!") || 
                    word.endsWith("?") || 
                    word.endsWith("...")            
                )) { 
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
        
        if (publication.isBlank()) { 
            return "все слова из переданного текста уже есть в словаре данного пользователя"; 
        }

        String result = addLessonToBase(title, publication, currentUser); 

        //String result = publication; 
        return result; 
    } 


    // метод для добавления созданного урока в базу 
    private String addLessonToBase(String title, String publication, User user) { 
        Long numberOfNewLesson; 

        Optional<Lesson> lessonOp = lessonService.getLatestLessonForUser(user); 
        if (lessonOp.isPresent()) {
            Lesson latestLesson = lessonOp.get(); 
            Long number = latestLesson.getNumber(); 
            numberOfNewLesson = number + 1;             
        } else { 
            numberOfNewLesson = 1L; 
        }
        
        String newLesson = 
            "Урок № " + numberOfNewLesson + 
            " пользователя " + user.getUsername() + 
            "\n" + "\n" + "\n" + publication; 

        try {
            lessonService.createLesson(numberOfNewLesson, title, newLesson, user); 
        } catch (Exception e) {
            System.out.println("при создании урока что-то пошло не так"); 
        }

        return newLesson; 
    }

}

package testgroup.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
        int lengthOfTitle = 8; 
        int lengthOfContextVocabulary = 30; 

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
        int contextVocCounter = 0; 
        int titleCounter = 0; 

        for (String word : words) { 
            String execWord = word
                    .replaceAll("[\\p{Punct}\\s–—]+", " ")
                    .trim() 
                    .toLowerCase();    
                    
            fragment = fragment + word + " "; 

            if (titleCounter < lengthOfTitle) {
                title = title + word + " "; 
                titleCounter++; 
            }
            
            Optional<Word> wordInBaseOp = Optional.empty(); 
            try { 
                wordInBaseOp = wordService.getWordByRusWordAndUser(execWord, currentUser); 
            } catch (Exception e) { 
                //e.printStackTrace(); 
                System.out.println("какая-то проблема с поиском слова в базе");                     
            }               

            if (!wordInBaseOp.isPresent()) {
                contextVocCounter++; 
                String translatedWord = translate(execWord); 
                try {
                    wordService.createWord(execWord, translatedWord, currentUser); 
                } catch (Exception e) { 
                    //e.printStackTrace(); 
                    System.out.println("какая-то проблема с сохранением слова в базу");                     
                }   
                contextVocabulary = 
                    contextVocabulary + execWord + " - " + translatedWord + "\n"; 
            } 

            if (contextVocCounter >= lengthOfContextVocabulary && (
                    word.endsWith(".") || 
                    word.endsWith("!") || 
                    word.endsWith("?") || 
                    word.endsWith("...")            
                )) { 
                contextVocCounter = 0; 
                publication = publication 
                    + contextVocabulary + "\n\n" 
                    + fragment + "\n\n" 
                    + "===========================================================" 
                    + "\n\n"; 
                contextVocabulary = ""; 
                fragment = ""; 
            } 
        } 
        
        if (publication.isBlank()) { 
            return "все слова из переданного текста уже есть в словаре данного пользователя"; 
        }

        String result = addLessonToBase(title, publication, currentUser); 
        return result; 
    } 


    /*     
    // метод для составления текста урока 
    public String makeLesson(String originText, String nameOfCurrentUser) {         
        String fragment = ""; 
        String contextVocabulary = ""; 
        String publication = ""; 
        String title = ""; 
        int lengthOfTitle = 8; 
        int lengthOfContextVocabulary = 30; 

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
        int contextVocCounter = 0; 
        int titleCounter = 0; 

        for (String word : words) { 
            String execWord = word
                    .replaceAll("[\\p{Punct}\\s–—]+", " ")
                    .trim() 
                    .toLowerCase();    
                    
            fragment = fragment + word + " "; 

            if (titleCounter < lengthOfTitle) {
                title = title + word + " "; 
                titleCounter++; 
            }
            
            Optional<Word> wordInBaseOp = Optional.empty(); 
            try { 
                wordInBaseOp = wordService.getWordByRusWordAndUser(execWord, currentUser); 
            } catch (Exception e) { 
                //e.printStackTrace(); 
                System.out.println("какая-то проблема с поиском слова в базе");                     
            }               

            if (!wordInBaseOp.isPresent()) {
                contextVocCounter++; 
                String translatedWord = translate(execWord); 
                try {
                    wordService.createWord(execWord, translatedWord, currentUser); 
                } catch (Exception e) { 
                    //e.printStackTrace(); 
                    System.out.println("какая-то проблема с сохранением слова в базу");                     
                }   
                contextVocabulary = 
                    contextVocabulary + execWord + " - " + translatedWord + "\n"; 
            } 

            if (contextVocCounter >= lengthOfContextVocabulary && (
                    word.endsWith(".") || 
                    word.endsWith("!") || 
                    word.endsWith("?") || 
                    word.endsWith("...")            
                )) { 
                contextVocCounter = 0; 
                publication = publication 
                    + contextVocabulary + "\n\n" 
                    + fragment + "\n\n" 
                    + "===========================================================" 
                    + "\n\n"; 
                contextVocabulary = ""; 
                fragment = ""; 
            } 
        } 
        
        if (publication.isBlank()) { 
            return "все слова из переданного текста уже есть в словаре данного пользователя"; 
        }

        String result = addLessonToBase(title, publication, currentUser); 
        return result; 
    } 
    */


    // метод для добавления созданного урока в базу 
    private String addLessonToBase(String title, String publication, User user) { 
        System.out.println("method addLessonToBase() started");
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
            "===========================================================" + "\n" +  
            "Урок № " + numberOfNewLesson + 
            " пользователя " + user.getUsername() + "\n" + 
            "===========================================================" + "\n\n\n" + 
            publication; 

        try {
            lessonService.createLesson(numberOfNewLesson, title, newLesson, user); 
        } catch (Exception e) {
            System.out.println("при создании урока что-то пошло не так"); 
        }

        return newLesson; 
    }


    // метод для отображения пользовательского словаря 
    public String showVocabulary(String username) { 

        User user = new User(); 
        Optional<User> userInBaseOp = userService.getUserByUsername(username); 
        List<String> pairs = new ArrayList<>(); 
        List<Word> list = new ArrayList<>();

        if (userInBaseOp.isPresent()) { 
            user = userInBaseOp.get(); 
        } 

        try {
            list = wordService.getAllWordsForUser(user);
        } catch (Exception e) {
            System.out.println("при поиске словаря что-то пошло не так"); 
            return "Cловарь пользователя не найден"; 
        }
        
        for (Word word : list) { 
            String rusWord = word.getRusWord(); 
            String engWord = word.getEngWord(); 
            String pair = rusWord + " - " + engWord; 
            pairs.add(pair); 
        } 

        Collections.sort(pairs); 
        String userVocabulary = String.join("\n", pairs); 

        if (userVocabulary.isBlank()) {
            return "Cловарь пользователя не найден";
        }
        return userVocabulary;         
    } 


    // метод для отображения пользовательского словаря в виде таблицы
    public List<Word> showVocabularyAsTable(String username) { 

        User user = new User(); 
        Optional<User> userInBaseOp = userService.getUserByUsername(username); 
        List<Word> words = new ArrayList<>();

        if (userInBaseOp.isPresent()) { 
            user = userInBaseOp.get(); 
        } 

        try {
            words = wordService.getAllWordsForUser(user);
        } catch (Exception e) {
            System.out.println("при поиске словаря что-то пошло не так"); 
        }
        
        Collections.sort(words, Comparator.comparing(Word::getRusWord)); 
        return words;               
    } 

} 
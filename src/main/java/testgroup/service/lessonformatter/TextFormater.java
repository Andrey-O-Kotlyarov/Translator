package testgroup.service.lessonformatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import testgroup.model.dao.LessonDao;
import testgroup.model.dao.UserDao;
import testgroup.model.dao.WordDao;
import testgroup.model.entity.Lesson;
import testgroup.model.entity.User;
import testgroup.model.entity.Word;
import testgroup.service.outerservices.LemmatizerOuterService;
import testgroup.service.translator.YandexTranslator; 

@Service
public class TextFormater { 

    @Autowired
    private WordDao wordService;  
    
    @Autowired
    private UserDao userService; 

    @Autowired
    private LessonDao lessonService; 

    @Autowired
    private LemmatizerOuterService lemmatizer; 

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
        String partForTranslator = ""; 

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
                    String iamToken = YandexTranslator.generIAMToken();    
                    String translatedWord = 
                        YandexTranslator.translate(execWord, "en", iamToken);                 
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

        // а если пользователь найден, 
        // то будем все новые слова добавлять в базу для этого пользователя: 

        // берем этого пользователя         
        User currentUser = userOptional.get(); 

        // устанавливаем начальные состояния счетчиков
        int contextVocCounter = 0; 
        int titleCounter = 0; 

        // получаем ключ для работы переводчика
        String iamToken = YandexTranslator.generIAMToken();     

        // берем массив, состоящий из слов исходного текста от пробела до пробела
        for (String word : words) { 

            // каждое такое слово из исходного массива добавляем в переводимый фрагмент
            // и восемь из них в заголовок
            fragment = fragment + word + " "; 

            if (titleCounter < lengthOfTitle) {
                title = title + word + " "; 
                titleCounter++; 
            }

            // обрезаем знаки препинания, делаем строчные буквы 
            String execWord = word
                    .replaceAll("[\\p{Punct}\\s–—«»]+", " ")
                    .trim() 
                    .toLowerCase();  
                    
            // если получившееся слово не содержит букв
            // или состоит из одних цифр или знаков препинания - пропускаем его
            if (execWord.isBlank() || 
                execWord.matches("\\d+") || 
                execWord.matches("^[\\p{Punct}]+$")) { 

                continue; 
            } 
                    
            // делаем лемматизацию каждого слова
            // чтобы каждый новый падеж не расценивался в качестве уникального слова, 
            // которого нет в базе             
            String lemma = lemmatizer.getLemma(execWord); 
                 
            // после лемматизации проверяем наличие слова в базе
            Optional<Word> wordInBaseOp = Optional.empty(); 
            try { 
                wordInBaseOp = wordService.getWordByRusWordAndUser(lemma, currentUser); 
            } catch (Exception e) { 
                //e.printStackTrace(); 
                System.out.println("какая-то проблема с поиском слова в базе");                     
            }               

            // если слова в базе нет, то добавляем его в стопку уникальных слов 
            // и инкрементируем счетчик 
            if (!wordInBaseOp.isPresent()) {
                contextVocCounter++;  
                partForTranslator = partForTranslator + "\n\n" + lemma; 
            } 
            
            // когда в этой стопке наберется тридцать слов 
            // и будет достигнут конец предложения 
            // то сбрасываем счетчик и отправляем стопку слов в переводчик 
            if (contextVocCounter >= lengthOfContextVocabulary && (
                    word.endsWith(".") || 
                    word.endsWith("!") || 
                    word.endsWith("?") || 
                    word.endsWith("...")            
                )) { 
                contextVocCounter = 0; 
                         
                String translatedPart = YandexTranslator.translate(
                    partForTranslator, "en", iamToken); 
                //System.out.println(partForTranslator);
                //System.out.println(translatedPart); 

                // стопку переведенных слов из переводчика преобразуем в массив
                // стопку непереведенных слов тоже преобразуем в массив
                String[] untranslatedWords = partForTranslator.split("\\r?\\n"); 
                String[] translatedWords = translatedPart.split("\\r?\\n"); 

                // берем слово из непереведенного массива и слово из переведенного
                // добавляем эту пару слов в контекстный словарь фрагмента 
                // и эту же пару добавляем в базу для данного пользователя 
                for (int i = 0; i < untranslatedWords.length; i++) {
                    String sWord = untranslatedWords[i]; 
                    if (sWord.isBlank()) {
                        continue;
                    }
                    System.out.println("\n" + sWord); 
                    String tWord = translatedWords[i].toLowerCase();  
                    System.out.println(tWord);                    
                    contextVocabulary = 
                        contextVocabulary + sWord + " - " + tWord + "\n"; 

                    try {
                        wordService.createWord(sWord, tWord, currentUser); 
                    } catch (Exception e) { 
                        //e.printStackTrace(); 
                        System.out.println("какая-то проблема с сохранением слова в базу \n");                     
                    } 
                } 
                
                // берем фрагмент исходного текста и контекстный словарь к нему 
                // добавляем их в содержимое урока, разделяя пустыми строками и полосками 
                // рабочие переменные сбрасываем 
                publication = publication 
                    + contextVocabulary + "\n\n" 
                    + fragment + "\n\n" 
                    + "===========================================================" 
                    + "\n\n\n"; 
                contextVocabulary = ""; 
                fragment = ""; 
                partForTranslator = "";
            } 
        } 
        
        // если в итоге в содержимом урока ничего нет, то возвращаем сообщение об этом
        // а если содержимое есть, 
        // то добавляем его вместе с заголовком в базу уроков данного пользователя 
        // и возвращаем готовый текст урока, сделанный методом-добавлятором
        if (publication.isBlank()) { 
            return "все слова из переданного текста уже есть в словаре данного пользователя"; 
        } 
        String result = addLessonToBase(title, publication, currentUser); 
        return result; 
    } 
    

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
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
import testgroup.service.lemmatizer.LemmatizerOuterService;
import testgroup.service.translator.YandexTranslator; 

@Service
public class TextFormater { 

    @Autowired
    private WordDao wordDao;  
    
    @Autowired
    private UserDao userDao; 

    @Autowired
    private LessonDao lessonDao; 

    @Autowired
    private LemmatizerOuterService lemmatizer; 

    // метод для составления текста урока 
    public Optional<Lesson> makeLesson(String originText, String nameOfCurrentUser) {         
        String fragment = ""; 
        String contextVocabulary = ""; 
        List<WordPair> contextVoc = new ArrayList<>(); 
        List<LessonUnit> unitList = new ArrayList<>(); 
        String publication = ""; 
        String title = ""; 
        int lengthOfTitle = 8; 
        int lengthOfContextVocabulary = 30; 

        String[] words = originText.split("[\s\r\n]+"); 
        Optional<User> userOptional = userDao.getUserByUsername(nameOfCurrentUser); 
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
            //return publication; 
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
                    .replaceAll("[\\p{Punct}\\s–—«»\"'“”‘’]+", " ")
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
                wordInBaseOp = wordDao.getWordByRusWordAndUser(lemma, currentUser); 
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
                System.out.println(partForTranslator);
                System.out.println(translatedPart); 

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
                    //contextVocabulary = 
                    //    contextVocabulary + sWord + " - " + tWord + "\n"; 
                    WordPair pair = new WordPair(sWord, tWord); 
                    contextVoc.add(pair); 
                    try {
                        wordDao.createWord(sWord, tWord, currentUser); 
                    } catch (Exception e) { 
                        //e.printStackTrace(); 
                        System.out.println("какая-то проблема с сохранением слова в базу \n");                     
                    } 
                } 

                // создаем переведенный фрагмент
                String translatedFragment = YandexTranslator.translate(
                    fragment, "en", iamToken); 
                
                // берем контекстный словарь к фрагменту
                // берем сам фрагмент и его перевод 
                // создаем из них юнит урока и добавляем этот юнит в содержимое урока
                // рабочие переменные сбрасываем 
                LessonUnit unit = new LessonUnit(contextVoc, fragment, translatedFragment); 
                unitList.add(unit); 

                contextVoc = new ArrayList<>();  
                contextVocabulary = ""; 
                fragment = ""; 
                partForTranslator = ""; 
            } 
        } 
        
        // если в итоге в содержимом урока ничего нет, то создаем сообщение об этом
        // помещаем это сообщение в тайтл урока и возвращаем этот урок
        // не добавляя его в базу 
        try { 
            String test = unitList.get(0).getTextFragment(); 
        } catch (Exception e) { 
            String note = 
                "Все слова из переданного текста уже есть в словаре данного пользователя " + "\n" 
                + "или объем текста слишком мал для создания урока"; 
            Lesson newLesson = new Lesson(); 
            newLesson.setTitle(note); 
            return Optional.of(newLesson); 
        } 

            // это пока не работает
        // а если в содержимом что-то есть, то создаем из него урок, помещаем его в базу 
        // и возвращаем этот урок из базы        
        //return addLessonToBase(title, unitList, currentUser); 
        Lesson newLesson = new Lesson(); 
        newLesson.setNumber(1L); 
        newLesson.setTitle("бла бла"); 
        newLesson.setContent(unitList); 
        newLesson.setUser(currentUser); 
        return Optional.of(newLesson); 
    } 
    

    // метод для добавления созданного урока в базу 
    private Optional<Lesson> addLessonToBase(String title, List<LessonUnit> content, User user) { 
        System.out.println("method addLessonToBase() started");
        Long numberOfNewLesson;  
        
        // берем из базы последний урок 
        // если он есть, то берем из него номер и увеличиваем на один 
        // а если уроков нет, то делаем номер равным единице         
        Optional<Lesson> lessonOp = lessonDao.getLatestLessonForUser(user); 
        if (lessonOp.isPresent()) {
            Lesson latestLesson = lessonOp.get(); 
            Long number = latestLesson.getNumber(); 
            numberOfNewLesson = number + 1;             
        } else { 
            numberOfNewLesson = 1L; 
        } 

        // создаем в базе новый урок с полученным номером
        // и этот созданный урок сразу извлекаем из базы и возвращаем
        Optional<Lesson> savedLessonOp = Optional.empty(); 
        try {
            Long lessonID = lessonDao.createLesson(numberOfNewLesson, title, content, user); 
            savedLessonOp = lessonDao.getLessonById(lessonID); 
        } catch (Exception e) {
            System.out.println("при создании урока что-то пошло не так"); 
        }

        return savedLessonOp; 
    }


    // метод для отображения пользовательского словаря 
    public String showVocabulary(String username) { 

        User user = new User(); 
        Optional<User> userInBaseOp = userDao.getUserByUsername(username); 
        List<String> pairs = new ArrayList<>(); 
        List<Word> list = new ArrayList<>();

        if (userInBaseOp.isPresent()) { 
            user = userInBaseOp.get(); 
        } 

        try {
            list = wordDao.getAllWordsForUser(user);
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
        Optional<User> userInBaseOp = userDao.getUserByUsername(username); 
        List<Word> words = new ArrayList<>();

        if (userInBaseOp.isPresent()) { 
            user = userInBaseOp.get(); 
        } 

        try {
            words = wordDao.getAllWordsForUser(user);
        } catch (Exception e) {
            System.out.println("при поиске словаря что-то пошло не так"); 
        }
        
        Collections.sort(words, Comparator.comparing(Word::getRusWord)); 
        return words;               
    } 


} 
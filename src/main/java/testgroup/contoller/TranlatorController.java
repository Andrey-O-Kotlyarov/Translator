package testgroup.contoller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.servlet.ModelAndView;
import testgroup.model.dao.LessonDao;
import testgroup.model.dao.UserDao;
import testgroup.model.dao.WordDao;
import testgroup.model.dto.JsonDTO;
import testgroup.model.entity.Lesson;
import testgroup.model.entity.User;
import testgroup.model.entity.Word;
import testgroup.service.filemanager.FileTypeChecker;
import testgroup.service.lessonformatter.LessonUnit;
import testgroup.service.lessonformatter.TextFormater;
import testgroup.service.screenrecognizer.SelenScreener;
import testgroup.service.screenrecognizer.TessRecognizer; 

@Controller
public class TranlatorController { 
    
    @Autowired
    private UserDao userDao;  

    @Autowired
    private WordDao wordDao; 

    @Autowired
    private LessonDao lessonDao; 

    @Autowired
    private TextFormater textFormater; 



    //заглавная страница
    @GetMapping(value = "/index")
    public ModelAndView showIndex(
            @RequestParam(name = "userName", required = false) String userName) {  

        System.out.println("controller /index started"); 
        
        String insertingText = "Приступаем"; 
        String nameOfCurrentUser = " "; 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        modelAndView.addObject("content", insertingText); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView;
    }
    

    //создание картинки и переход на страницу с картинкой
    @GetMapping(value = "/showScreen")
    public ModelAndView showScreen(
            @RequestParam(name = "userName", required = false) String userName, 
            @RequestParam(name = "screeningPageURL") String screeningPageURL, 
            @RequestParam(name = "bigSize") boolean bigSize) { 

        System.out.println("controller /showScreen started"); 

        String timestamp = Long.toString(System.currentTimeMillis()); 
        String pictureFile = 
            "src\\main\\resources\\static\\screens\\screenshot_" + timestamp + ".png";
        //String serverAccessToPictureFile = "/screens/screenshot_" + timestamp + ".png";  
        String nameOfCurrentUser = " "; 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        
        if (screeningPageURL == null || screeningPageURL.isBlank()) {  
            System.out.println("URL не передан");            
            modelAndView.addObject("content", 
                "Похоже, что Вы не ввели URL или ввели его некорректно " + "\n" + 
                "Попробуйте еще раз"); 
            modelAndView.addObject("userName", nameOfCurrentUser); 
            return modelAndView; 
        }
        
        SelenScreener.screenPage(screeningPageURL, pictureFile, bigSize); 
        System.out.println("Screening is done"); 
        
        File fileObject = new File(pictureFile);
        String justTheFileName = fileObject.getName();
        
        modelAndView.addObject("imageFileName", justTheFileName); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    }  


    //страница с картинкой
    @GetMapping(value = "/getImage")
    public ResponseEntity<byte[]> getImage(            
            //@RequestParam(name = "userName", required = false) String userName, 
            @RequestParam("fileName") String fileName) throws IOException { 

        System.out.println("controller /getImage started");
    
        // Путь к папке, где лежат скриншоты
        Path imagePath = Paths.get("src\\main\\resources\\static\\screens", fileName);
        //String nameOfCurrentUser = " "; 

        //if(userName != null && !userName.isEmpty()) { 
        //    nameOfCurrentUser = userName; 
        //} 
    
        if (!Files.exists(imagePath)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Картинки нет
        }

        // Читаем файл в массив байтов
        byte[] imageContent = Files.readAllBytes(imagePath);

        // Формируем ответ
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG); // Указываем тип контента

        return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
    } 


    //получение от клиента картинки для распознавания текста на ней
    @PostMapping(value = "/uploadScreenForRecognizing")
    public ResponseEntity<String> uploadScreenForRecognizing(
            @RequestParam(name = "userName", required = false) String userName, 
            @RequestBody JsonDTO request) throws Exception { 

        System.out.println("controller /uploadScreenForRecognizing started"); 
        String pictureForTess = "src\\main\\resources\\static\\pictureForTess.png"; 
        String nameOfCurrentUser = " "; 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 
        
        Path path = Paths.get(pictureForTess); 
        try { 
            Files.delete(path); 
        } catch (Exception e) {
            e.printStackTrace();
        }         
        Files.createFile(path); 
        
        try {
            // Отрезаем префикс "data:image/png;base64,"
            String encodedImage = 
                request.getContent().substring(request.getContent().indexOf(',')+1); 

            // Декодируем base64 в массив байтов   
            byte[] decodedBytes = Base64.getDecoder().decode(encodedImage);         
            Files.write(path, decodedBytes);  
        } catch (Exception e) {
            System.out.println("хуйня какая-то"); 
        }   
        
        //это все равно не отображается, вместо этого будет переход на /showRecognizedText 
        return ResponseEntity.ok("ok");      
    }

 
    //переход на страницу с текстом
    @GetMapping(value = "/showRecognizedText")
    public ModelAndView showRecognizedText(
            @RequestParam(name = "userName", required = false) String userName) {  

        System.out.println("controller /showRecognizedText started"); 
        String pictureForTess = "src\\main\\resources\\static\\pictureForTess.png";  
        String textFile = "src\\main\\resources\\static\\output.txt";  
        String nameOfCurrentUser = " "; 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index"); 

        try {
            boolean isPNG = FileTypeChecker.checkPNG(pictureForTess);             
            if (!isPNG) {
                System.out.println("файл не является png");                 
                modelAndView.addObject("content", 
                    "Прежде чем распознавать, нужно что-то отсканировать"); 
                modelAndView.addObject("userName", nameOfCurrentUser);                     
                return modelAndView; 
            }
        } catch (Exception e) {
            System.out.println("какая-то проблема с проверкой типа файла"); 
        }   

        TessRecognizer.recognizeText(pictureForTess, textFile); 
        System.out.println("Recognizing is done"); 

        String recognizedText = TessRecognizer.textConvert(textFile); 
        String halfPreparedText = TessRecognizer.deleteFirstLine(recognizedText); 
        String preparedText = TessRecognizer.deleteLastLine(halfPreparedText); 

        modelAndView.addObject("content", preparedText); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    } 


    //форма регистрации и входа
    @GetMapping(value = "/showLoginPage")
    public ModelAndView showLoginPage(
            @RequestParam(name = "userName", required = false) String userName) {   
        
        System.out.println("controller /showLoginPage started");  

        String nameOfCurrentUser = " "; 
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("loginPage"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    }


    //вход существующего пользователя 
    @PostMapping(value = "/logIn")
    public ModelAndView logIn (
            @RequestParam(name = "userName", required = false) String userName, 
            @RequestParam("username") String username,
            @RequestParam("pass") String pass) {   
        
        System.out.println("controller /logIn started"); 

        String nameOfCurrentUser = " "; 
        Optional<User> userInBaseOp = 
            userDao.getUserByUsernameAndPass(username, pass); 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        if (!userInBaseOp.isPresent()) { 
            System.out.println("Пользователь не найден в базе"); 

            ModelAndView modelAndView = new ModelAndView(); 
            modelAndView.setViewName("loginPage"); 
            modelAndView.addObject("content", "Пользователь не найден");
            return modelAndView;
        } 

        nameOfCurrentUser = userInBaseOp.get().getUsername(); 
        System.out.println("Пользователь " + nameOfCurrentUser + " найден в базе" );
          
        ModelAndView modelAndView = new ModelAndView(); 
        modelAndView.setViewName("index"); 
        modelAndView.addObject("content", "Добро пожаловать, " + nameOfCurrentUser + "!"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    } 


    //форма создания нового пользователя 
    @PostMapping(value = "/showRegistrationPage")
    public ModelAndView showRegistrationPage (
            @RequestParam(name = "userName", required = false) String userName) {   
        
        System.out.println("controller /showRegistrationPage started"); 
        String nameOfCurrentUser = " ";  

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();            
        modelAndView.setViewName("registrationPage"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    }


    //создание нового пользователя в базе
    @PostMapping(value = "/createAcc")
    public ModelAndView createAcc (
            @RequestParam(name = "userName", required = false) String userName, 
            @RequestParam("mail") String mail,
            @RequestParam("username") String username,
            @RequestParam("pass") String pass) {   
        
        System.out.println("controller /createAcc started"); 

        Long id; 
        String nameOfCurrentUser = " "; 
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 
        
        ModelAndView modelAndView = new ModelAndView();  

        try {
            id = userDao.createUser(mail, username, pass); 
        } catch (Exception e) {
            System.out.println("Такой пользователь уже есть в базе"); 

            modelAndView.setViewName("registrationPage"); 
            modelAndView.addObject("content", "Такой пользователь уже зарегистрирован"); 
            modelAndView.addObject("userName", nameOfCurrentUser); 
            return modelAndView;
        }          

        Optional<User> userInBaseOp = userDao.getUserById(id); 
        if (!userInBaseOp.isPresent()) { 
            System.out.println("При регистрации что-то пошло не так"); 

            modelAndView.setViewName("registrationPage");             
            modelAndView.addObject("content", "При регистрации что-то пошло не так"); 
            modelAndView.addObject("userName", nameOfCurrentUser); 
            return modelAndView;
        } 
       
        nameOfCurrentUser = userInBaseOp.get().getUsername(); 
        System.out.println("Новый пользователь " + nameOfCurrentUser + " внесен в базу" );

        modelAndView.setViewName("index"); 
        modelAndView.addObject("content", "Добро пожаловать, " + nameOfCurrentUser + "!"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView;
    }


    //разлогинивание юзера
    @PostMapping(value = "/logOut")
    public ModelAndView logOut(
            @RequestParam(name = "userName", required = false) String userName) {   
        
        System.out.println("controller /logOut started"); 

        String nameOfCurrentUser = ""; 
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        String logoutedUserName = nameOfCurrentUser;
        nameOfCurrentUser = "";

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        modelAndView.addObject("content", "Пользователь " + logoutedUserName + " разлогинился"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    }


    //создание урока
    @PostMapping(value = "/createLesson")
    public ResponseEntity<String> createLesson(
            @RequestParam(name = "userName", required = false) String userName, 
            @RequestBody JsonDTO request) throws Exception {   
        
        System.out.println("controller /createLesson started"); 
        
        String textFromUser = "src\\main\\resources\\static\\textFromUser.txt"; 
        String nameOfCurrentUser = " "; 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 
        
        Path path = Paths.get(textFromUser); 
        try { 
            Files.delete(path); 
        } catch (Exception e) {
            e.printStackTrace();
        }         
        Files.createFile(path); 

        String requestText = request.getContent(); 
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(requestText);
            System.out.println("Файл записан успешно ");
        } catch (IOException e) {
            System.err.println("Ошибка при записи файла: " + e.getMessage());
        } 
        
        //это все равно не будет показано, вместо этого будет переход на /showlesson 
        return ResponseEntity.ok("ok");      
    }


    //отображение созданного урока 
    @GetMapping(value = "/showLesson")
    public ModelAndView showLesson(
            @RequestParam(name = "userName", required = false) String userName) {   
        
        System.out.println("controller /showLesson started");  
        
        String filePath = "src\\main\\resources\\static\\textFromUser.txt"; 
        String content = ""; 
        String nameOfCurrentUser = " "; 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        try {
            content = Files
                .lines(Paths.get(filePath))
                .collect(Collectors.joining("\n")); 
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        
        Lesson lesson = null; 
        Long lessonNumber = null;  
        String lessonTitle = "";  
        List<LessonUnit> lessonContent = null; 

        Optional<Lesson> lessonOp = 
            textFormater.makeLesson(content, nameOfCurrentUser); 
        if(lessonOp.isPresent()) { 
            lesson = lessonOp.get();
            lessonNumber = lesson.getNumber();  
            lessonTitle = lesson.getTitle();  
            lessonContent = lesson.getContent(); 
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index"); 
        modelAndView.addObject("lessonNumber", lessonNumber); 
        modelAndView.addObject("lessonTitle", lessonTitle); 
        modelAndView.addObject("lessonContent", lessonContent);         
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    }

  
    //страница с уроками пользователя
    @GetMapping(value = "/showLessonList")
    public ModelAndView showLessonList(
            @RequestParam(name = "userName", required = false) String userName) {   
        
        System.out.println("controller /showLessonList started"); 

        List<Lesson> lessonList = null; 
        String nameOfCurrentUser = " "; 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 

        if(nameOfCurrentUser.isBlank() || nameOfCurrentUser == null) { 
            String note = "Зарегистрируйтесь, чтобы посмотреть список Ваших уроков";             
            modelAndView.addObject("content", note);             
            return modelAndView; 
        }

        try {
            User currentUser = userDao.getUserByUsername(nameOfCurrentUser).get();
            lessonList = lessonDao.getAllLessonsForUser(currentUser); 
        } catch(Exception e) {
            System.out.println("какие-то проблемы с получением списка уроков"); 
            e.printStackTrace(); 
        } 

        if(lessonList == null || lessonList.isEmpty()) { 
            String note = "У пользователя " + nameOfCurrentUser + " еще нет уроков";             
            modelAndView.addObject("content", note);             
            return modelAndView; 
        }
        
        modelAndView.addObject("lessonList", lessonList);         
        return modelAndView; 
    } 


    //страница с подтверждением удаления урока
    @PostMapping(value = "/showDeleteLessonPage/{id}")
    public ModelAndView showDeleteLessonPage(
            @RequestParam(name = "userName", required = false) String userName, 
            @PathVariable Long id) {   
        
        System.out.println("controller /showDeleteLessonPage started");   
        String nameOfCurrentUser = " ";    
        
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("deleteLessonPage");    
        modelAndView.addObject("lessonId", id);     
        modelAndView.addObject("userName", nameOfCurrentUser);     
        return modelAndView; 
    } 


    //действительно удаление урока
    @PostMapping(value = "/reallyDeleteLesson")
    public ModelAndView reallyDeleteLesson(
            @RequestParam(name = "userName", required = false) String userName, 
            @RequestParam(name = "lessonId") String lessonId) {   
        
        System.out.println("controller /reallyDeleteLesson started");     
        String nameOfCurrentUser = " ";   
        
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        try {
            Long number = Long.parseLong(lessonId); 
            lessonDao.deleteLesson(number);
        } catch(NumberFormatException e) {
            System.out.println("Не удалось удалить урок"); 
        }          

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/showLessonList"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    } 


    //отображение выбранного урока из списка
    @PostMapping(value = "/showSelectedLesson/{id}")
    public ModelAndView showSelectedLesson(
            @RequestParam(name = "userName", required = false) String userName, 
            @PathVariable Long id) {   
        
        System.out.println("controller /showSelectedLesson started"); 
        System.out.println(id); 
        
        Lesson lesson = null; 
        Long lessonNumber = null;  
        String lessonTitle = "";  
        List<LessonUnit> lessonContent = null; 
        String nameOfCurrentUser = " "; 

        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 
        
        Optional<Lesson> lessonOp = lessonDao.getLessonById(id); 
        if(lessonOp.isPresent()) { 
            lesson = lessonOp.get(); 
            lessonNumber = lesson.getNumber();  
            lessonTitle = lesson.getTitle();  
            lessonContent = lesson.getContent(); 
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        modelAndView.addObject("lessonNumber", lessonNumber); 
        modelAndView.addObject("lessonTitle", lessonTitle); 
        modelAndView.addObject("lessonContent", lessonContent);         
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    }
    

    //пользовательский словарь
    @PostMapping(value = "/showVocabulary")
    public ModelAndView showVocabulary(
            @RequestParam(name = "userName", required = false) String userName) {   
        
        System.out.println("controller /showVocabulary started");  

        String nameOfCurrentUser = " "; 
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 

        if(nameOfCurrentUser.isBlank() || nameOfCurrentUser == null) { 
            String note = "Зарегистрируйтесь, чтобы посмотреть Ваш словарь ";             
            modelAndView.addObject("content", note);             
            return modelAndView; 
        }

        List<Word> wordList = textFormater.showVocabularyAsTable(nameOfCurrentUser); 

        if(wordList == null || wordList.isEmpty()) { 
            String note = 
                "У пользователя " + nameOfCurrentUser + " еще нет словаря, или он был удален " + 
                "\n" + "Чтобы словарь появился, создайте урок ";             
            modelAndView.addObject("content", note);             
            return modelAndView; 
        } 
        
        modelAndView.addObject("words", wordList);         
        return modelAndView; 
    } 

    
    //удаление слова 
    @PostMapping(value = "/deleteWord/{id}")
    public ModelAndView deleteWord (
            @RequestParam(name = "userName", required = false) String userName, 
            @PathVariable Long id, 
            @RequestParam("scrollPosition") String position) { 
                
        System.out.println("controller /deleteWord started");         
        System.out.println("позиция прокрутки " + position); 

        String nameOfCurrentUser = " "; 
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 
        
        double number = 1; 
        try {
            number =Double.parseDouble(position); 
        } catch(NumberFormatException e) {
            System.out.println("Строка не является числом"); 
        } 
       
        wordDao.deleteWord(id); 
        List<Word> wordList = textFormater.showVocabularyAsTable(nameOfCurrentUser); 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index"); 
        modelAndView.addObject("words", wordList); 
        modelAndView.addObject("scrollInfo", number);
        modelAndView.addObject("userName", nameOfCurrentUser); 
        return modelAndView; 
    } 


    //страница с подтверждением удаления всего словаря 
    @PostMapping(value = "/showDeleteVocabularyPage")
    public ModelAndView showDeleteVocabularyPage(
            @RequestParam(name = "userName", required = false) String userName) {   
        
        System.out.println("controller /showDeleteVocabularyPage started");   

        String nameOfCurrentUser = " ";     
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        Long userId = null; 

        Optional<User> userOp = userDao.getUserByUsername(userName);         
        if(userOp.isPresent()) { 
            userId = userOp.get().getId(); 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("deleteVocabularyPage");    
        modelAndView.addObject("userId", userId);  
        modelAndView.addObject("userName", nameOfCurrentUser);        
        return modelAndView; 
    } 


    //удаление всего словаря 
    @PostMapping(value = "/deleteVocabulary")
    public ModelAndView deleteVocabulary(
            @RequestParam(name = "userName", required = false) String userName, 
            @RequestParam(name = "userId") String userId) {   
        
        System.out.println("controller /deleteVocabulary started");  
        
        String nameOfCurrentUser = " "; 
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        try {
            Long number = Long.parseLong(userId);             
            wordDao.deleteByUser_Id(number); 
        } catch(NumberFormatException e) {
            System.out.println("Не удалось удалить словарь"); 
        } 

        String note = "Словарь пользователя " + nameOfCurrentUser + " удален"; 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        modelAndView.addObject("content", note);         
        return modelAndView; 
    } 


    //выбор языка 
    @PostMapping(value = "/selectLanguage")
    public ModelAndView selectLanguage(
            @RequestParam(name = "userName", required = false) String userName) {   
        
        System.out.println("controller //selectLanguage started");        
        
        String note = "Здесь будет реализована опция выбора языка " + "\n" + 
            "Сейчас доступен по умолчанию только английский "; 

        String nameOfCurrentUser = " "; 
        if(userName != null && !userName.isEmpty()) { 
            nameOfCurrentUser = userName; 
        } 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index"); 
        modelAndView.addObject("userName", nameOfCurrentUser); 
        modelAndView.addObject("content", note);         
        return modelAndView; 
    } 


} 
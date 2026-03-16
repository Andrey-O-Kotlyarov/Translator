package testgroup.contoller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.servlet.ModelAndView; 
import testgroup.dto.JsonDTO;
import testgroup.model.User;
import testgroup.model.Word;
import testgroup.service.FileTypeChecker;
import testgroup.service.SelenScreener;
import testgroup.service.TessRecognizer;
import testgroup.service.TextFormater;
import testgroup.service.UserService;
import testgroup.service.WordService; 

@Controller
public class TranlatorController { 
    
    @Autowired
    private UserService userService;  

    @Autowired
    private WordService wordService; 

    @Autowired
    private TextFormater textFormater; 
    
    private String nameOfCurrentUser = ""; 
    
    
    //заглавная страница
    @GetMapping(value = "/index")
    public ModelAndView showApplication4() {  

        System.out.println("controller /index started"); 
        String insertingText = "Приступаем"; 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4");
        modelAndView.addObject("content", insertingText); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView;
    }
    

    //создание картинки и переход на страницу с картинкой
    @GetMapping(value = "/screen")
    public ModelAndView showApplication4WithScreen(
            @RequestParam(name = "login") String login, 
            @RequestParam(name = "bigSize") boolean bigSize) { 

        System.out.println("controller /screen started");

        String sourceURL = login; 
        String timestamp = Long.toString(System.currentTimeMillis()); 
        String pictureFile = 
            "src\\main\\resources\\static\\screens\\screenshot_" + timestamp + ".png";
        String serverAccessToPictureFile = "/screens/screenshot_" + timestamp + ".png";  

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4");
        
        if (sourceURL == null || sourceURL.isBlank()) {  
            System.out.println("URL не передан");            
            modelAndView.addObject("content", 
                "Похоже, что Вы не ввели URL или ввели его некорректно " + "\n" + 
                "Попробуйте еще раз"); 
            modelAndView.addObject("user", nameOfCurrentUser); 
            return modelAndView; 
        }
        
        SelenScreener.screenPage(sourceURL, pictureFile, bigSize); 
        System.out.println("Screening is done"); 
        
        modelAndView.addObject("imagePath", serverAccessToPictureFile); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    }     


    //метод получения изображения 
    @PostMapping(value = "/upload_image")
    public ResponseEntity<String> handleImageUpload(@RequestBody JsonDTO request) throws Exception { 

        System.out.println("controller /upload_image started"); 
        String pictureForTess = "src\\main\\resources\\static\\pictureForTess.png"; 
        
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
        
        //это все равно не отображается, вместо этого будет переход на /text 
        return ResponseEntity.ok("ok");      
    }

 
    //переход на страницу с текстом
    @GetMapping(value = "/text")
    public ModelAndView showNewScreen() {  

        System.out.println("controller /text started"); 
        String pictureForTess = "src\\main\\resources\\static\\pictureForTess.png";  
        String textFile = "src\\main\\resources\\static\\output.txt";  

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4"); 

        try {
            boolean isPNG = FileTypeChecker.checkPNG(pictureForTess);             
            if (!isPNG) {
                System.out.println("файл не является png");                 
                modelAndView.addObject("content", 
                    "Прежде чем распознавать, нужно что-то отсканировать"); 
                modelAndView.addObject("user", nameOfCurrentUser);                     
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
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    } 


    //форма регистрации и входа
    @GetMapping(value = "/regform")
    public ModelAndView getUserName() {   
        
        System.out.println("controller /regform started");  
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("regform"); 
        return modelAndView; 
    }


    //вход существующего пользователя 
    @PostMapping(value = "/olduser")
    public ModelAndView doOldUserIn(
            @RequestParam("username") String username,
            @RequestParam("pass") String pass) {   
        
        System.out.println("controller /olduser started"); 
        Optional<User> userInBaseOp = 
            userService.getUserByUsernameAndPass(username, pass); 

        if (!userInBaseOp.isPresent()) { 
            System.out.println("Пользователь не найден в базе"); 

            ModelAndView modelAndView = new ModelAndView(); 
            modelAndView.setViewName("regform"); 
            modelAndView.addObject("content", "Пользователь не найден");
            return modelAndView;
        } 

        nameOfCurrentUser = userInBaseOp.get().getUsername(); 
        System.out.println("Пользователь " + nameOfCurrentUser + " найден в базе" );
          
        ModelAndView modelAndView = new ModelAndView(); 
        modelAndView.setViewName("application4"); 
        modelAndView.addObject("content", "Добро пожаловать, " + nameOfCurrentUser + "!"); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    } 


    //форма создания нового пользователя 
    @PostMapping(value = "/newuser")
    public ModelAndView doNewUserIn() {   
        
        System.out.println("controller /newuser started");  
        ModelAndView modelAndView = new ModelAndView();            
        modelAndView.setViewName("regnewuser"); 
        return modelAndView; 
    }


    //создание нового пользователя в базе
    @PostMapping(value = "/createacc")
    public ModelAndView createNewAcc(
            @RequestParam("mail") String mail,
            @RequestParam("username") String username,
            @RequestParam("pass") String pass) {   
        
        System.out.println("controller /createacc started"); 
        Long id; 
        ModelAndView modelAndView = new ModelAndView();  

        try {
            id = userService.createUser(mail, username, pass); 
        } catch (Exception e) {
            System.out.println("Такой пользователь уже есть в базе"); 

            modelAndView.setViewName("regnewuser"); 
            modelAndView.addObject("content", "Такой пользователь уже зарегистрирован");
            return modelAndView;
        }          

        Optional<User> userInBaseOp = userService.getUserById(id); 
        if (!userInBaseOp.isPresent()) { 
            System.out.println("При регистрации что-то пошло не так"); 

            modelAndView.setViewName("regnewuser");             
            modelAndView.addObject("content", "При регистрации что-то пошло не так");
            return modelAndView;
        } 
       
        nameOfCurrentUser = userInBaseOp.get().getUsername(); 
        System.out.println("Новый пользователь " + nameOfCurrentUser + " внесен в базу" );

        modelAndView.setViewName("application4"); 
        modelAndView.addObject("content", "Добро пожаловать, " + nameOfCurrentUser + "!"); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView;
    }


    //разлогинивание юзера
    @PostMapping(value = "/getout")
    public ModelAndView logout() {   
        
        System.out.println("controller /getout started"); 
        String logoutedUserName = nameOfCurrentUser;
        nameOfCurrentUser = "";

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4");
        modelAndView.addObject("content", "Пользователь " + logoutedUserName + " разлогинился"); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    }


    //создание урока
    @PostMapping(value = "/createlesson")
    public ResponseEntity<String> createLesson(@RequestBody JsonDTO request) throws Exception {   
        
        System.out.println("controller /createlesson started"); 
        String textFromUser = "src\\main\\resources\\static\\textFromUser.txt"; 
        
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
    @GetMapping(value = "/showlesson")
    public ModelAndView showLesson() {   
        
        System.out.println("controller /showlesson started");  
        String filePath = "src\\main\\resources\\static\\textFromUser.txt"; 
        String content = "";

        try {
            content = Files
                .lines(Paths.get(filePath))
                .collect(Collectors.joining("\n")); 
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        String lesson = textFormater.makeLesson(content, nameOfCurrentUser); 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4");
        modelAndView.addObject("content", lesson); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    }

  
    //страница с уроками пользователя
    @PostMapping(value = "/lessons")
    public ModelAndView getLessons() {   
        
        System.out.println("controller /lessons started");                       
        String insertingText = "здесь будут уроки пользователя";

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4");
        modelAndView.addObject("content", insertingText); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    }
    

    //пользовательский словарь
    @PostMapping(value = "/vocabulary")
    public ModelAndView getVocabulary() {   
        
        System.out.println("controller /vocabulary started");  
        List<Word> list = textFormater.showVocabularyAsTable(nameOfCurrentUser); 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4");     
        modelAndView.addObject("words", list);
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    } 


    //удаление слова 
    @PostMapping(value = "/delete/{id}")
    public ModelAndView deleteWord(
            @PathVariable Long id, 
            @RequestParam("scrollPosition") String position) { 
                
        System.out.println("controller /delete started");         
        System.out.println("позиция прокрутки " + position); 
        
        int number = 1; 
        try {
            number = Integer.parseInt(position); 
        } catch(NumberFormatException e) {
            System.out.println("Строка не является числом"); 
        } 
       
        wordService.deleteWord(id); 
        List<Word> list = textFormater.showVocabularyAsTable(nameOfCurrentUser); 

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4"); 
        modelAndView.addObject("words", list); 
        modelAndView.addObject("scrollInfo", number);
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    } 
} 
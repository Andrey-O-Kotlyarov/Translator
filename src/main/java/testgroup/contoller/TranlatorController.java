package testgroup.contoller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.servlet.ModelAndView; 
import testgroup.model.User;
import testgroup.service.FileTypeChecker;
import testgroup.service.SelenScreener;
import testgroup.service.TessRecognizer;
import testgroup.service.UserService; 

@Controller
public class TranlatorController { 
    
    @Autowired
    private UserService userService;  
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


    //метод получения изображения 
    @PostMapping("/upload_image")
    public ModelAndView handleImageUpload(@RequestBody ImageDto request) throws Exception { 

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
                request.getImageBase64().substring(request.getImageBase64().indexOf(',')+1); 

            // Декодируем base64 в массив байтов   
            byte[] decodedBytes = Base64.getDecoder().decode(encodedImage);         
            Files.write(path, decodedBytes);  
        } catch (Exception e) {
            System.out.println("хуйня какая-то"); 
        }   
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4"); 
        modelAndView.addObject("content", "Изображение успешно загружено"); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView;         
    }

    
    //DTO для передачи изображения от клиента
    static class ImageDto {
        private String imageBase64;

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }
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
        String name = username; 
        String password = pass; 

        //делаем поиск по имени и паролю сразу
        Optional<User> userOptional = userService.getUserByUsernameAndPass(name, password); 

        if (userOptional.isPresent()) {
            System.out.println("Пользователь найден в базе" );
        } else {
            System.out.println("Пользователь не найден в базе"); 

            ModelAndView modelAndView = new ModelAndView(); 
            modelAndView.setViewName("regform"); 
            modelAndView.addObject("content", "Пользователь не найден");
            return modelAndView;
        } 

        User registredUser = userOptional.get(); 
        String nameOfRegistredUser = registredUser.getUsername(); 
        nameOfCurrentUser = nameOfRegistredUser; 

        ModelAndView modelAndView = new ModelAndView();            
        modelAndView.setViewName("application4"); 
        modelAndView.addObject("content", "Добро пожаловать, " + nameOfRegistredUser + "!"); 
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

        /*Сохраняем пользователя в базе данных. Этот метод делает следующее: 
          - создает новую энтити класса Юзер 
          - заполняет ее поля мейлом, юзернеймом и паролем 
          - вызывает у JpaRepository метод save 
          При этом используются классы: 
          - Юзер - это энтити, 
          - JpaRepository - набор методов для переноса энтити в базу и обратно, 
          - ЮзерСервис - его метод вызывает метод Jpa репозитория, а перед этим 
            создает и заполняет энтити для прередачи в метод репозитория 
        */
        try {
            id = userService.createUser(mail, username, pass); 
        } catch (Exception e) {
            System.out.println("Такой пользователь уже есть в базе"); 

            modelAndView.setViewName("regnewuser"); 
            modelAndView.addObject("content", "Такой пользователь уже зарегистрирован");
            return modelAndView;
        }          

        Optional<User> userOptional = userService.getUserById(id); 
        if (userOptional.isPresent()) {
            System.out.println("Новый пользователь внесен в базу" );
        } else {
            System.out.println("При регистрации что-то пошло не так"); 

            modelAndView.setViewName("regnewuser");             
            modelAndView.addObject("content", "При регистрации что-то пошло не так");
            return modelAndView;
        } 

        User registredUser = userOptional.get(); 
        String nameOfRegistredUser = registredUser.getUsername(); 
        nameOfCurrentUser = nameOfRegistredUser; 

        // Перенаправляем на страницу успеха
        //return "redirect:/success";         
        
        modelAndView.setViewName("application4"); 
        modelAndView.addObject("content", "Добро пожаловать, " + nameOfRegistredUser + "!"); 
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


    //страница с уроками
    @PostMapping(value = "/study")
    public ModelAndView getLessons() {   
        
        System.out.println("controller /study started");                       
        String insertingText = "здесь будут готовые уроки";

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("application4");
        modelAndView.addObject("content", insertingText); 
        modelAndView.addObject("user", nameOfCurrentUser); 
        return modelAndView; 
    }
    

}
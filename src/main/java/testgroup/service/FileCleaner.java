package testgroup.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class FileCleaner { 
    
    //метод для удаления файла
    public static void deleteFile(String pathToFile) { 
        try { 
            Path fileToDelete = Path.of(pathToFile); 
            Files.delete(fileToDelete); 
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }


    // метод для однократной очистки папки
    public static void cleanFolder(String folderPath) {
        try {
            File directory = new File(folderPath);             
            if (!directory.exists() || !directory.isDirectory()) {
                System.out.println("Директория не существует или неверный путь.");
                return;
            }
            
            for (File file : directory.listFiles()) {
                Files.deleteIfExists(file.toPath());
            }            
            System.out.println("Файлы успешно удалены из директории: " + folderPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // метод для очистки папки с заданной периодичностью
    public static void startFileCleaning(String folderPath, int periodInMinutes) {

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> cleanFolder(folderPath),
                0,
                periodInMinutes,
                TimeUnit.MINUTES);
    }

}
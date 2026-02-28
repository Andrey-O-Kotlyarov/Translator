package testgroup.service; 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException; 
import org.springframework.stereotype.Service; 
import net.sourceforge.tess4j.*; 

@Service
public class TessRecognizer { 

    //метод для преобразования картинки в текстовый файл
    public static void recognizeText(String sourcePictureFile, String destTextFile) {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/langmodels"); // Директория с файлами *.traineddata
        tesseract.setLanguage("rus"); // русский язык
        //tesseract.setVariable("user_defined_dpi", "300");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destTextFile))) {  
            File source = new File(sourcePictureFile);
            String result = tesseract.doOCR(source);             
            writer.write(result); // Запись строки в файл
        } catch (Exception e) {
            System.out.println("то ли метод распознания картинку не получил, то ли еще чего"); 
        }
    }

    //метод для преобразования текстового файла в строку
    public static String textConvert(String sourceTextFile) {
        String insertingText = "";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceTextFile))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    content.append("\n"); 
                } else {
                    firstLine = false;
                }
                
                content.append(line);
            }
        } catch (IOException e) {
            System.out.println("какие-то проблемы у преобразования в строку");
            //e.printStackTrace();
        } 

        insertingText = content.toString(); 
        return insertingText; 
    }


    // метод для удаления первой строки
    public static String deleteFirstLine(String sourceString) { 
        String result = "";
        int index = sourceString.indexOf('\n');
        
        if (index >= 0) {
            result = sourceString.substring(index + 1); // берем все после первого '\n' 
        } else {
            System.out.println("нет первой строки");
        } 

        return result; 
    }


    // метод для удаления последней строки 
    public static String deleteLastLine(String sourceString) {
        String result = ""; 
        int lastNewlineIndex = sourceString.lastIndexOf('\n');
        
        if (lastNewlineIndex >= 0 && lastNewlineIndex < sourceString.length() - 1) {
            result = sourceString.substring(0, lastNewlineIndex); 
        } else {
            System.out.println("нет последней строки");
        } 
        return result; 
    }
} 
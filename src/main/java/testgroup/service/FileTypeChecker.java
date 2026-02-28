package testgroup.service;

import java.io.FileInputStream;
import java.io.IOException; 
import org.springframework.stereotype.Service;

@Service
public class FileTypeChecker {

    // метод для проверки типа файла, является ли он png
    public static boolean checkPNG(String pathToCheckingFile) throws IOException {
        byte[] PNG_SIGNATURE = 
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}; 

        try (FileInputStream fis = new FileInputStream(pathToCheckingFile)) {
            byte[] header = new byte[PNG_SIGNATURE.length];
            fis.read(header);
            for (int i = 0; i < PNG_SIGNATURE.length; i++) {
                if (header[i] != PNG_SIGNATURE[i]) {
                    return false;
                }
            }
            return true;
        }
    }

} 

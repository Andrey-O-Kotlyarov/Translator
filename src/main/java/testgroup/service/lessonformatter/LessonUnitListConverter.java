package testgroup.service.lessonformatter; 

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.List;

@Converter
public class LessonUnitListConverter implements AttributeConverter<List<LessonUnit>, String> { 

    private final ObjectMapper objectMapper = new ObjectMapper(); 

    
    // метод для преобразования списка объектов LessonUnit в JSON-строку
    @Override
    public String convertToDatabaseColumn(List<LessonUnit> units) {
        try {
            
            return objectMapper.writeValueAsString(units);
        } catch (JsonProcessingException e) {
            // Обработка ошибки
            throw new RuntimeException("Error converting LessonUnit list to JSON", e);
        }
    }


    // метод для преобразования JSON-строки в список объектов LessonUnit
    @Override
    public List<LessonUnit> convertToEntityAttribute(String dbData) {
        try {
            
            if (dbData == null || dbData.isEmpty()) {
                return List.of(); // Возвращаем пустой список
            }
            return objectMapper.readValue(dbData, new TypeReference<List<LessonUnit>>(){});
        } catch (IOException e) {
            // Обработка ошибки
            throw new RuntimeException("Error converting JSON to LessonUnit list", e);
        }
    } 


} 
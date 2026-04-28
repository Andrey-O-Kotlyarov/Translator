package testgroup.service; 

import com.google.gson.annotations.SerializedName;
import java.util.List; 

public class TranslateResponse { 

    @SerializedName("translations")
    List<TranslationItem> translations;

    public static class TranslationItem {
        @SerializedName("text")
        String text;
        
        @SerializedName("detectedLanguageCode")
        String detectedLanguageCode;
    } 
} 
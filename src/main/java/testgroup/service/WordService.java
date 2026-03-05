package testgroup.service;

import java.util.Optional; 
import testgroup.model.User;
import testgroup.model.Word;

public interface WordService { 
    public Long createWord(String rusWord, String engWord, User user); 
    public Optional<Word> getWordByRusWord (String rusWord); 
}

package testgroup.model.dao;

import java.util.List;
import java.util.Optional; 
import testgroup.model.entity.User;
import testgroup.model.entity.Word;

public interface WordDao { 
    public Long createWord(String rusWord, String engWord, User user); 
    public Optional<Word> getWordByRusWord (String rusWord); 
    public Optional<Word> getWordByRusWordAndUser (String rusWord, User user); 
    public List<Word> getAllWordsForUser(User user); 
    public List<Word> getAllWordsForUserId(Long userId); 
    public void deleteWord(Long id); 
    public void deleteByUser(User user); 
    public void deleteByUser_Id(Long userId); 

} 
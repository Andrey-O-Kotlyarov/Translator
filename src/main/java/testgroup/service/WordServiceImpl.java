package testgroup.service;

import java.util.List;
import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import testgroup.dao.WordRepository; 
import testgroup.model.User;
import testgroup.model.Word;

@Service
public class WordServiceImpl implements WordService { 

    @Autowired
    private WordRepository wordRepository; 

    @Override
    public Long createWord(String rusWord, String engWord, User user) {
        Word word = new Word();
        word.setRusWord(rusWord); 
        word.setEngWord(engWord); 
        word.setUser(user); 
        
        Word savedWord = wordRepository.save(word);         
        Long id = savedWord.getId(); 
        return id; 
    } 

    @Override
    public Optional<Word> getWordByRusWord (String rusWord) {  
        Optional<Word> op = wordRepository.findByRusWord(rusWord);          
        return op; 
    } 

    @Override
    public Optional<Word> getWordByRusWordAndUser (String rusWord, User user) {
        Optional<Word> op = wordRepository.findByRusWordAndUser(rusWord, user); 
        return op; 
    }


    @Override
    public List<Word> getAllWordsForUser(User user) {
        return wordRepository.findAllByUser(user);
    }


    @Override
    public List<Word> getAllWordsForUserId(Long userId) {
        return wordRepository.findAllByUser_Id(userId);
    }  


    @Override
    @SuppressWarnings("null")
    public void deleteWord(Long id) {
        wordRepository.deleteById(id);
    }


} 
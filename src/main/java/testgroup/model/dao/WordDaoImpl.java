package testgroup.model.dao;

import java.util.List;
import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; 
import testgroup.model.entity.User;
import testgroup.model.entity.Word;
import testgroup.model.repository.WordRepository;

@Repository
public class WordDaoImpl implements WordDao { 

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


    @Override
    @Transactional
    public void deleteByUser(User user) { 
        wordRepository.deleteByUser(user); 
    }


    @Override
    @Transactional
    public void deleteByUser_Id(Long userId) { 
        wordRepository.deleteByUser_Id(userId); 
    } 


} 
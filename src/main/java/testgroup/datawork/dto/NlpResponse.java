package testgroup.datawork.dto; 

import java.util.List;

public class NlpResponse { 
    
    // Список токенов. В JSON это поле называется "tokens"
    private List<Token> tokens;

    // Геттер, чтобы мы могли получить доступ к списку
    public List<Token> getTokens() {
        return tokens;
    }

    // Setter нужен для работы ObjectMapper
    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    } 
}

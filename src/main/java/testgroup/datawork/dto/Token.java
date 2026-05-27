package testgroup.datawork.dto;

public class Token { 
    private String text;
    private String lemma; // Нам нужно только это поле

    public String getLemma() {
        return lemma;
    }

    public String getText() {
        return this.text;
    }

    // Setter нужен для работы ObjectMapper
    public void setLemma(String lemma) {
        this.lemma = lemma;
    } 

    public void setText(String text) {
        this.text = text;
    }

}

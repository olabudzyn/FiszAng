package com.budzynska.fiszang.basedata;

public class DictionaryElement {

    private String elementId;
    private String englishWord;
    private String polishWord;

    public DictionaryElement(){}

    public DictionaryElement(String elementId, String englishWord, String polishWord) {
        this.elementId = elementId;
        this.englishWord = englishWord;
        this.polishWord = polishWord;
    }

    public String getElementId() {
        return elementId;
    }

    public String getEnglishWord() {
        return englishWord;
    }

    public String getPolishWord() {
        return polishWord;
    }
}

package com.budzynska.fiszang.basedata;

public class Dictionary {

    private String dictionaryName;
    private String dictionaryId;

    public Dictionary(){}

    public Dictionary(String dictionaryName, String dictionaryId) {
        this.dictionaryName = dictionaryName;
        this.dictionaryId = dictionaryId;
    }

    public String getDictionaryName() {
        return dictionaryName;
    }

    public String getDictionaryId() {
        return dictionaryId;
    }
}

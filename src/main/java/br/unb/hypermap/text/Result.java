package br.unb.hypermap.text;

import java.io.Serializable;
import java.util.Map;

public class Result implements Serializable {

    private static final long serialVersionUID = 7539032571851608499L;

    private String id;

    private Map<String, Integer> keywords;

    private double score;

    public Result() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Integer> getKeywords() {
        return keywords;
    }

    public void setKeywords(Map<String, Integer> keywords) {
        this.keywords = keywords;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}

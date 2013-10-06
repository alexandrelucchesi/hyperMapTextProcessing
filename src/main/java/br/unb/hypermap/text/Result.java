package br.unb.hypermap.text;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class Result implements Serializable {

    private static final long serialVersionUID = 7539032571851608499L;

    private String id;

    private Map<String, Integer> keywords;

    private double score;
    
    private Qualifier quality;

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
    
    public Qualifier getQuality() {
		return quality;
	}

	public void setQuality(Qualifier quality) {
		this.quality = quality;
	}

    public Result sortKeywords() {
        ValueComparator comparator = new ValueComparator(keywords);
        SortedMap<String, Integer> sortedKeywords = new TreeMap<String, Integer>(comparator);
        sortedKeywords.putAll(keywords);
        this.keywords = sortedKeywords;
        return this;
    }

	private class ValueComparator implements Comparator<String>, Serializable {

        private static final long serialVersionUID = 4976424122864432064L;
		
        private final Map<String,Integer> keywords;

        public ValueComparator(Map<String, Integer> keywords) {
            this.keywords = keywords;
        }

        @Override
        public int compare(String s, String s2) {
            if (keywords.get(s) >= keywords.get(s2)) {
                return -1;
            } else {
                return 1;
            } // return 0 would merge keys
        }
    }

}

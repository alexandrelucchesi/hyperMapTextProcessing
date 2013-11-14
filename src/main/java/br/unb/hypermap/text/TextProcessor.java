package br.unb.hypermap.text;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TextProcessor {

    private static final String CONTENTS = "contents";

    private Set<String> terms = new HashSet<String>();

    /**
     * Computes the cosine similarity between a given query and a collection of data.
     *
     * @param query User query.
     * @param data  Associates an id to data.
     */
    public HashSet<Result> process(String query, Map<String, String> data) throws IOException {
        if (data == null)
            return null;

        HashSet<Result> results = new HashSet<Result>();

        Set<String> keys = data.keySet();
        for (String id : keys) { // Repeats for each data source...
            /* Instantiates a new set to hold the terms */
            terms = new HashSet<String>();

            /* Builds an index containing two documents holding the query and data contents */
            Directory directory = createIndex(query, data.get(id));
            if(directory == null) continue;
            IndexReader reader = DirectoryReader.open(directory);

            /* Gets term frequencies for both documents */
            Map<String, Integer> f1 = getTermFrequencies(reader, 0);
            Map<String, Integer> f2 = getTermFrequencies(reader, 1);

            /* Closes the index */
            reader.close();

            /* Makes real vectors from the terms previously retrieved */
            RealVector v1 = toRealVector(f1);
            RealVector v2 = toRealVector(f2);

            /* Adds the result of the computation to the set of results */
            Result result = new Result();
            result.setId(id);
            result.setKeywords(f2);
            result.setScore(getCosineSimilarity(v1, v2));
            results.add(result);
        }

        return results;
    }

    private Directory createIndex(String s1, String s2) throws IOException {
        if(s2.length() == 0) return null;
        Directory directory = new RAMDirectory();
        Analyzer analyzer = new HyperMapAnalyzer(Version.LUCENE_44);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44,
                analyzer);
        IndexWriter writer = new IndexWriter(directory, iwc);
        System.out.println("s1 length: " + s1.length() + "s2 length: " + s2.length());
        addDocument(writer, s1);
        addDocument(writer, s2);
        writer.close();
        return directory;
    }

    /* Indexed, tokenized, stored. */
    private static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_STORED.setIndexed(true);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.setStoreTermVectorPositions(true);
        TYPE_STORED.freeze();
    }

    private void addDocument(IndexWriter writer, String contents) throws IOException {
        Document doc = new Document();
        Field field = new Field(CONTENTS, contents, TYPE_STORED);
        doc.add(field);
        writer.addDocument(doc);
    }

    private Map<String, Integer> getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, CONTENTS);
        System.out.println("vector: " + vector + " docId: " + docId);
        TermsEnum termsEnum = null;
        termsEnum = vector.iterator(termsEnum);
        Map<String, Integer> frequencies = new HashMap<String, Integer>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            int freq = (int) termsEnum.totalTermFreq();
            frequencies.put(term, freq);
            terms.add(term);
        }
        return frequencies;
    }

    private RealVector toRealVector(Map<String, Integer> map) {
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        for (String term : terms) {
            int value = map.containsKey(term) ? map.get(term) : 0;
            vector.setEntry(i++, value);
        }
        return (RealVector) vector.mapDivide(vector.getL1Norm());
    }

    private double getCosineSimilarity(RealVector v1, RealVector v2) {
        return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
    }

}


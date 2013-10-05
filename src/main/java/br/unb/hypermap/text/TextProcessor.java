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

/**
 * Created with IntelliJ IDEA.
 * User: alexandrelucchesi
 * Date: 10/5/13
 * Time: 6:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextProcessor {

    public static TextProcessor textProcessor;

    public static final String CONTENTS = "contents";

    private final Set<String> terms = new HashSet<String>();

    public static TextProcessor instance() {
        if (textProcessor == null)
            textProcessor = new TextProcessor();
        return textProcessor;
    }

    private TextProcessor() {}

    public Result process(String query, String data) throws IOException {
        Directory directory = createIndex(query, data);
        IndexReader reader = DirectoryReader.open(directory);
        Map<String, Integer> f1 = getTermFrequencies(reader, 0);
        Map<String, Integer> f2 = getTermFrequencies(reader, 1);
        reader.close();
        RealVector v1 = toRealVector(f1);
        RealVector v2 = toRealVector(f2);
        Result result = new Result();
        result.setKeywords(f2);
        result.setScore(getCosineSimilarity(v1, v2));
        return result;
    }

    public Set<Result> processAll(String query, Set<String> data) throws IOException {
        Set<Result> results = new HashSet<Result>();
        for (String s : data) {
            results.add(process(query, s));
        }
        return results;
    }

    private Directory createIndex(String s1, String s2) throws IOException {
        Directory directory = new RAMDirectory();
        Analyzer analyzer = new HyperMapAnalyzer(Version.LUCENE_44);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44,
                analyzer);
        IndexWriter writer = new IndexWriter(directory, iwc);
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

    private double getCosineSimilarity(RealVector v1, RealVector v2) {
        return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
    }

    private Map<String, Integer> getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, CONTENTS);
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

}

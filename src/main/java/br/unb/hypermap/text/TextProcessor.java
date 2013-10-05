package br.unb.hypermap.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class TextProcessor {

    private Analyzer analyzer;

    private IndexWriter writer;

    private Directory indexDir;

    public TextProcessor(Analyzer analyzer, File indexDir) throws IOException {
        Directory dir = indexDir != null ? FSDirectory.open(indexDir) : new RAMDirectory();
        this.analyzer = analyzer;
        this.indexDir = dir;
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, analyzer);
        this.writer = new IndexWriter(dir, config);
    }

    public TextProcessor(File indexDir) throws IOException {
        this(new HyperMapAnalyzer(Version.LUCENE_44), indexDir);
    }

    public void index(String id, String data, boolean store) throws IOException {
        Document document = new Document();
        document.add(new StoredField("id", id));
        FieldType fieldType = new FieldType();
        fieldType.setIndexed(true);
        fieldType.setOmitNorms(true);
        fieldType.setTokenized(true);
        fieldType.setStored(store);
        fieldType.setStoreTermVectors(true);
        Field contents = new Field("contents", data, fieldType);
        contents.setBoost(1.0f);
        document.add(contents);
        writer.addDocument(document);
        writer.commit();
    }

    public void index(String id, File file, boolean store) throws IOException {
        this.index(id, readFile(file), store);
    }

    private String readFile(File file) throws IOException {
        return new Scanner(file).useDelimiter("\\A").next();
    }

    public Set<Result> search(String query, int n) throws IOException, ParseException {
        IndexReader reader = null;
        QueryParser parser = new QueryParser(Version.LUCENE_44, "contents", new HyperMapAnalyzer(Version.LUCENE_44));
        Query q = parser.parse(query);

        int hitsPerPage = n;
        reader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        Set<Result> results = new HashSet<Result>();
        for (int i = 0; i < hits.length; i++) {
            int docID = hits[i].doc;
            Document d = searcher.doc(docID);
            Result r = new Result();
            r.setId(d.get("id"));

            Map<String, Integer> keywords = new HashMap<String, Integer>();
            Terms terms = reader.getTermVector(docID, "contents"); //get terms vectors for one document and one field
            if (terms != null && terms.size() > 0) {
                TermsEnum termsEnum = terms.iterator(null); // access the terms for this field
                BytesRef term = null;
                for (int j = 0; (term = termsEnum.next()) != null; j++) {// explore the terms for this field
                    DocsEnum docsEnum = termsEnum.docs(null, null); // enumerate through documents, in this case only one
                    int docIdEnum;
                    while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        keywords.put(term.utf8ToString(), docsEnum.freq());
                    }
                }
            }

            ValueComparator comparator = new ValueComparator(keywords);
            SortedMap<String, Integer> sortedKeywords = new TreeMap<String, Integer>(comparator);
            sortedKeywords.putAll(keywords);

            r.setKeywords(sortedKeywords);

            Explanation explanation = searcher.explain(q, docID);
            System.out.println(explanation.toString());

            results.add(r);
        }
        return results;
    }

    public void close() throws IOException {

    }

    public List<String> tokenizeString(Reader reader) {
        List<String> result = new ArrayList<String>();
        try {
            TokenStream stream = analyzer.tokenStream(null, reader);
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<String> tokenizeString(String text) {
        return tokenizeString(new StringReader(text));
    }

    private class ValueComparator implements Comparator<String> {

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

package br.unb.hypermap.text;

import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.Scanner;

public class PrecisionRecall {

    public static void main(String[] args) throws Throwable {

        // INDEXING...
        Directory directory = FSDirectory.open(new File("index"));
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, new HyperMapAnalyzer(Version.LUCENE_44));
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(directory, iwc);

        addDocument(writer, "apache1.0.txt", readFile(new File("samples/quality/apache1.0.txt")));
        addDocument(writer, "apache1.1.txt", readFile(new File("samples/quality/apache1.0.txt")));
        addDocument(writer, "apache2.0.txt", readFile(new File("samples/quality/apache1.0.txt")));
        addDocument(writer, "car.txt", readFile(new File("samples/car.txt")));
        addDocument(writer, "mobile.txt", readFile(new File("samples/mobile.txt")));
        addDocument(writer, "sell.txt", readFile(new File("samples/sell.txt")));

        writer.close();

        //----------------------------------------------------------------------

        // MEASURING QUALITY...
        File topicsFile = new File("quality/topics.txt");
        File qrelsFile = new File("quality/qrels.txt");
        Directory dir = FSDirectory.open(new File("index"));

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        PrintWriter logger = new PrintWriter(System.out, true);

        TrecTopicsReader qReader = new TrecTopicsReader();   //#1
        QualityQuery qqs[] = qReader.readQueries(            //#1
                new BufferedReader(new FileReader(topicsFile))); //#1

        System.out.println("QUALITY QUERIES");
        for (QualityQuery q : qqs) {
            System.out.println("ID: " + q.getQueryID());
            System.out.println("Name=Value:");
            for (String s : q.getNames()) {
                System.out.println("\t" + s + "=" + q.getValue(s));
            }
            System.out.println();
        }
        System.out.println();

        Judge judge = new TrecJudge(new BufferedReader(      //#2
                new FileReader(qrelsFile)));                     //#2

        judge.validateData(qqs, logger);                     //#3

        QualityQueryParser qqParser = new HyperMapQQAnalyzer("title", "contents");  //#4

        QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, URL);
        SubmissionReport submitLog = null;
        QualityStats stats[] = qrun.execute(judge,           //#5
                submitLog, logger);

        QualityStats avg = QualityStats.average(stats);      //#6
        avg.log("SUMMARY", 2, logger, "  ");
        dir.close();
    }


    /* Indexed, tokenized, stored. */
    private static final FieldType TYPE_STORED = new FieldType();

    private static final String CONTENTS = "contents";

    private static final String URL = "url";

    static {
        TYPE_STORED.setIndexed(true);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.setStoreTermVectorPositions(true);
        TYPE_STORED.freeze();
    }

    private static void addDocument(IndexWriter writer, String url, String contents) throws IOException {
        Document doc = new Document();
        Field f1 = new StringField(URL, url, Field.Store.YES);
        Field f2 = new Field(CONTENTS, contents, TYPE_STORED);
        doc.add(f1);
        doc.add(f2);
        writer.addDocument(doc);
    }

    /* Helper */
    private static String readFile(File file) throws IOException {
        return new Scanner(file).useDelimiter("\\A").next();
    }
}

/*
#1 Read TREC topics as QualityQuery[]
#2 Create Judge from TREC Qrel file
#3 Verify query and Judge match
#4 Create parser to translate queries into Lucene queries
#5 Run benchmark
#6 Print precision and recall measures
*/

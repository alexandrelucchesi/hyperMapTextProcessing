package br.unb.hypermap.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.benchmark.quality.Judge;
import org.apache.lucene.benchmark.quality.QualityBenchmark;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.QualityQueryParser;
import org.apache.lucene.benchmark.quality.QualityStats;
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

public class PrecisionRecall {
	
	private Directory indexDir;
	private IndexWriter writer;
	private File topicsFile;
	private File qrelsFile;
	private HashMap<String, ArrayList<String>> qrels;

	private static String TOPICS_FORMAT = "<top>\n" +
			"<num> Number: %d\n" +
			"<title> %s\n" +
			"<desc> Description: %s\n" +
			"<narr> Narrative: %s\n" +
			"</top>";
	//qnum	0	doc-name	is-relevant
	private static String QRELS_FORMAT = "%d\t0\t%s\t%d";
	
	public PrecisionRecall(File indexPath, File topicsFile, File qrelsFile) throws Throwable {
		indexDir = FSDirectory.open(indexPath);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, new HyperMapAnalyzer(Version.LUCENE_44));
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(indexDir, iwc);
        qrels = new HashMap<String, ArrayList<String>>();
        this.topicsFile = topicsFile;
        this.qrelsFile = qrelsFile; 
	}
	
	public void createQrelsAndTopics() throws IOException {
		PrintWriter qrelsOut = new PrintWriter(new FileWriter(qrelsFile));
		PrintWriter topicsOut = new PrintWriter(new FileWriter(topicsFile));
		
		Set<String> keySet = qrels.keySet();
		int qNum = 0;
		for(String key : keySet) {
			topicsOut.println(String.format(TOPICS_FORMAT, qNum, key, "", ""));
			topicsOut.flush();
			
			ArrayList<String> urls = qrels.get(key);
			for(int i = 0; i < urls.size(); i++) {
				qrelsOut.println(String.format(QRELS_FORMAT, qNum, urls.get(i), 1));
				qrelsOut.flush();
			}
			qNum++;
		}
		topicsOut.close();
		qrelsOut.close();
	}
	
	public void addRelevantDocument(String query, String url) {
		if(!qrels.containsKey(query)) {
			ArrayList<String> urls = new ArrayList<String>();
			urls.add(url);
			qrels.put(query, urls);
		} else {
			ArrayList<String> urls = qrels.get(query);
			urls.add(url);
			qrels.put(query, urls); // Is it unecessary? urls share the same instance of qrels.get(query).
		}
	}
	
	public void closeIndexWriter() throws Throwable {
		writer.close();
	}
	
	public void close() throws Throwable {
		indexDir.close();
	}

    public void getPrecisionRecall(PrintStream output) throws Throwable {

        // MEASURING QUALITY...
        IndexReader reader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(reader);

        PrintWriter logger = new PrintWriter(output, true);

        TrecTopicsReader qReader = new TrecTopicsReader();   //#1
        QualityQuery qqs[] = qReader.readQueries(new BufferedReader(new FileReader(topicsFile))); //#1

        System.out.println("QUALITY QUERIES ---------");
        for (QualityQuery q : qqs) {
            System.out.println("ID: " + q.getQueryID());
            System.out.println("Name=Value:");
            for (String s : q.getNames()) {
                System.out.println("\t" + s + "=" + q.getValue(s));
            }
            System.out.println();
        }
        System.out.println();

        Judge judge = new TrecJudge(new BufferedReader(new FileReader(qrelsFile))); //#2

        judge.validateData(qqs, logger);//#3

        QualityQueryParser qqParser = new HyperMapQQAnalyzer("title", "contents");  //#4

        QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, URL);
        SubmissionReport submitLog = null;
        QualityStats stats[] = qrun.execute(judge, submitLog, logger); //#5

        QualityStats avg = QualityStats.average(stats); //#6
        avg.log("SUMMARY", 2, logger, "  ");
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

    public void addDocument(String url, String contents) throws IOException {
        Document doc = new Document();
        Field f1 = new StringField(URL, url, Field.Store.YES);
        Field f2 = new Field(CONTENTS, contents, TYPE_STORED);
        doc.add(f1);
        doc.add(f2);
        writer.addDocument(doc);
    }
    
    public void addDocument(String url, File file) throws IOException {
        Document doc = new Document();
        Field f1 = new StringField(URL, url, Field.Store.YES);
        Field f2 = new Field(CONTENTS, readFile(file), TYPE_STORED);
        doc.add(f1);
        doc.add(f2);
        writer.addDocument(doc);
    }

    /* Helper */
    public String readFile(File file) throws IOException {
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

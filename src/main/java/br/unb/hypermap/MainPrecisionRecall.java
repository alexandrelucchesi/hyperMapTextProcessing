package br.unb.hypermap;

import java.io.File;
import java.io.PrintStream;

import br.unb.hypermap.text.PrecisionRecall;

public class MainPrecisionRecall {

	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		PrecisionRecall pr = new PrecisionRecall(new File("index"), new File("quality/topics2.txt"),
				new File("quality/qrels2.txt"));
		// INDEXING
		pr.addDocument("apache1.0.txt", new File("target/apache1.0.txt"));
        pr.addDocument("apache1.1.txt", new File("target/apache1.1.txt"));
        pr.addDocument("apache2.0.txt", new File("target/apache2.0.txt"));
        pr.addDocument("car.txt", new File("samples/car.txt"));
        pr.addDocument("mobile.txt", new File("samples/mobile.txt"));
        pr.addDocument("sell.txt", new File("samples/sell.txt"));
        pr.closeIndexWriter();
		
        // QUALITY
        pr.addRelevantDocument("apache source", "apache1.0.txt");
        pr.addRelevantDocument("apache source", "apache1.1.txt");
        pr.addRelevantDocument("apache source", "apache2.0.txt");
        pr.addRelevantDocument("brubles", "car.txt");
        pr.createQrelsAndTopics();
        
        pr.getPrecisionRecall(System.out);
        pr.getPrecisionRecall(new PrintStream(new File("target/out.txt")));
        
        pr.close();
        
	}

}

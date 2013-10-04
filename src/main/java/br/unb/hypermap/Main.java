package br.unb.hypermap;

import br.unb.hypermap.text.Result;
import br.unb.hypermap.text.TextProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {

        TextProcessing tp = new TextProcessing(null);

        tp.index("http://naisse.com", "we are so awesome! :D", true);
        tp.index("mobile.txt", new File("samples/mobile.txt"), true);
        tp.index("car.txt", new File("samples/car.txt"), true);

        while (true) {
            System.out.println("Enter query: ");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            java.lang.String query = bufferedReader.readLine();
            if (query.equals("q"))
                break;

            Set<Result> results = tp.search(query, 10);
            if (results.isEmpty()) {
                System.out.println("No matches found.");
                System.out.println();
            } else {
                for (Result r : results) {
                    System.out.println("URL     : " + r.getId());
                    System.out.println("Keywords: " + r.getKeywords());
                    System.out.println("Score   : " + r.getScore());
                    System.out.println();
                }
            }
        }

        tp.close();
    }

}

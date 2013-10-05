package br.unb.hypermap;

import br.unb.hypermap.text.Result;
import br.unb.hypermap.text.TextProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {

        /* Gets a TextProcessor instance */
        TextProcessor textProcessor = TextProcessor.instance();

        /* Builds the data source */
        Set<String> data = new HashSet<String>();
        data.add(readFile(new File("samples/mobile.txt")));
        data.add(readFile(new File("samples/car.txt")));
        data.add(readFile(new File("samples/sell.txt")));

        while (true) {
            /* Gets user query */
            System.out.println("Enter query: ");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            java.lang.String query = bufferedReader.readLine();
            if (query.equals("q"))
                break;

            /* Process query */
            Set<Result> results = textProcessor.processAll(query, data);

            /* Show the results */
            for (Result r : results) {
                System.out.println("URL     : " + r.getId());
                System.out.println("Keywords: " + r.sortKeywords().getKeywords());
                System.out.println("Score   : " + r.getScore());
                System.out.println();
            }
        }
    }

    /* Helper */
    private static String readFile(File file) throws IOException {
        return new Scanner(file).useDelimiter("\\A").next();
    }

}

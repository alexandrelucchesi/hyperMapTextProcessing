package br.unb.hypermap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import br.unb.hypermap.text.Result;
import br.unb.hypermap.text.TextProcessor;

public class Main {

    public static void main(String[] args) throws Exception {

        /* Instantiates a TextProcessor */
        TextProcessor textProcessor = new TextProcessor();

        /* Builds the data source */
        Map<String, String> data = new HashMap<String, String>();
        data.put("http://mobile.com", readFile(new File("samples/mobile.txt")));
        data.put("http://car.com", readFile(new File("samples/car.txt")));
        data.put("http://sell.com", readFile(new File("samples/sell.txt")));

        while (true) {
            /* Gets user query */
            System.out.print("Enter query: ");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            java.lang.String query = bufferedReader.readLine();
            if (query.equals("q"))
                break;

            /* Process query */
            Set<Result> results = textProcessor.process(query, data);

            System.out.println();
            if (results == null) {
                System.out.println("Oh gosh! You passed null data, didn't you?");
                System.out.println("You won't break my app this way, sry! :-( haha");
                break;
            } else {
                int count = 0;
                for (Result r : results) {
                    /* Show only results that matched */
                    if (r.getScore() > 0) {
                        System.out.println("URL     : " + r.getId());
                        System.out.println("Keywords: " + r.sortKeywords().getKeywords());
                        System.out.println("Score   : " + r.getScore());
                        System.out.println();
                        count++;
                    }
                }
                System.out.println("Total matches: " + count);
                System.out.println("=================================================================");
            }
        }
    }

    /* Helper */
    private static String readFile(File file) throws IOException {
        return new Scanner(file).useDelimiter("\\A").next();
    }

}

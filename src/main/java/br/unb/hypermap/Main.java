package br.unb.hypermap;

import br.unb.hypermap.text.Result;
import br.unb.hypermap.text.TProcessor;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {

        TProcessor tProcessor = TProcessor.instance();
        Set<String> data = new HashSet<String>();
        data.add(readFile(new File("samples/mobile.txt")));
        data.add(readFile(new File("samples/car.txt")));
        data.add(readFile(new File("samples/sell.txt")));

        while (true) {
            System.out.println("Enter query: ");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            java.lang.String query = bufferedReader.readLine();
            if (query.equals("q"))
                break;

            Set<Result> results = tProcessor.processAll(query, data);

            if (results.isEmpty()) {
                System.out.println("No matches found.");
                System.out.println();
            } else {
                for (Result r : results) {
                    System.out.println("URL     : " + r.getId());
                    System.out.println("Keywords: " + r.sortKeywords().getKeywords());
                    System.out.println("Score   : " + r.getScore());
                    System.out.println();
                }
            }
        }
    }

    private static String readFile(File file) throws IOException {
        return new Scanner(file).useDelimiter("\\A").next();
    }

}

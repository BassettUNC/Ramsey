package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Analytics {
    public static void main(String[] args) {
        //TODO: For now stock history must be preloaded, need to fetch automatically from online API.
        Stock AVEO = new StockImpl("src/analysis/data/AVEO.csv");
        Stock TSLA = new StockImpl("src/analysis/data/TSLA.csv");
        comparePrediction("src/analysis/data/Futures.csv");
    }

    public static void comparePrediction(String path) {

        // Create scanner object if file exists. Otherwise, die.
        Scanner csvReader = null;
        try {
            csvReader = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Initialize temporary ArrayLists.
        ArrayList<String> tempRow = new ArrayList<String>();
        ArrayList<Stock> tempTickers = new ArrayList<Stock>();

        int s = -1;
        int i = 0;
        String controlDate = null;
        String workingDate = null;
        while (csvReader.hasNextLine()) {
            // Create new scanner to traverse each line.
            Scanner csvLine = new Scanner(csvReader.nextLine());
            csvLine.useDelimiter(",");

            // Define first cell of each line as identifier. Will either be firm abbreviation OR ticker.
            String identifier = csvLine.next();

            //TODO: Determine why identifier length is different.
            //If identifier is firm abbreviation...
            if (identifier.length() == 3 || identifier.length() == 2) {
                while (csvLine.hasNext()) {
                    // Create stock object and add to list.
                    tempTickers.add(new StockImpl("src/analysis/data/" + csvLine.next() + ".csv"));
                }
                s = 0;
                // Otherwise... traverse row and perform evaluation.
            } else {
                // TODO: Control needs to be assigned from first given date.
                // TODO: 0% row needs to be skipped.
                if (identifier.equals("2019-01-14")) {
                    controlDate = identifier;
                }

                while(csvLine.hasNext()) {
                    if (i == 0) {
                        workingDate = identifier;
                    }
                    // If performance > predicted.
                    if (stockPerformance(controlDate, workingDate, tempTickers.get(i)) >= Double.parseDouble(csvLine.next())) {
                        System.out.println("Growth exceeds or meets prediction from " + controlDate + " to " + workingDate + ".");
                    } else {
                        System.out.println("Prediction exceeds growth from " + controlDate + " to " + workingDate + ".");
                    }
                    i ++;
                }
            }
            i = 0;
            csvLine.close();
            s ++;
        }
        csvReader.close();
    }

    // Return percent growth or loss. Requires start date, end date, and ticker.
    public static Double stockPerformance(String start, String stop, Stock ticker) {
        return (ticker.close(stop) - ticker.close(start))/ticker.close(start);
    }

}

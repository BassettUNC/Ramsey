package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Analytics {
    public static void main(String[] args) {
        //TODO: For now stock history must be preloaded, need to fetch automatically from online API.
        comparePrediction("src/analysis/data/Futures.csv");
    }

    public static void comparePrediction(String path) {
        // Create scanner line object if file exists. Otherwise, die.
        Scanner csvReader = null;
        try {
            csvReader = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Cannot find futures csv.");
            e.printStackTrace();
        }

        // Initialize temporary ArrayList and local variables.
        ArrayList<Stock> tempTickers = new ArrayList<>();
        ArrayList<String> allTickers = new ArrayList<>(); // Contains tickers of all created stock objects.
        Integer n = null; // Row number. Resets for each firm.
        int i = 0; // Cell number. Resets each row.
        String controlDate = null;
        String workingDate = null;

        while (csvReader.hasNextLine()) {
            // Create new scanner object to traverse each line.
            Scanner csvLine = new Scanner(csvReader.nextLine());
            csvLine.useDelimiter(",");

            // Define first cell of each line as identifier. Will either be firm abbreviation or date.
            String identifier = csvLine.next();
            // If identifier is firm abbreviation...
            // For unknown reason, length of first cell is one longer than it should be... or statement accounts for this.
            if ((identifier.length() == 3 && n == null) || identifier.length() == 2) {
                while (csvLine.hasNext()) {
                    String ticker = csvLine.next();

                    // If stock object doesn't already exist try creating new one from csv...
                    if(!allTickers.contains(ticker)) {
                        try {
                            allTickers.add(ticker);
                            // Create stock objects and add to list.
                            tempTickers.add(new StockImpl("src/analysis/data/" + ticker + ".csv"));
                        } catch(NullPointerException e) {
                            System.out.println("ERROR: Cannot find Stock csv file.");
                        }
                    }
                }
                n = 0;

                // If on 0% control row, set control date and move to next row.
            } else if (n == 1) {
                controlDate = identifier;

                // Otherwise, iterate through each cell and compare to predicted value.
            } else {
                try {
                    while(csvLine.hasNext()) {
                        if (i == 0) {
                            workingDate = identifier;
                        }
                        // If performance > predicted.
                        //TODO: Automatically find nearest date.
                        if (stockPerformance(controlDate, workingDate, tempTickers.get(i)) >= Double.parseDouble(csvLine.next())) {
                            System.out.println("Growth exceeds or meets prediction from " + controlDate + " to " + workingDate + ".");
                        } else {
                            System.out.println("Prediction exceeds growth from " + controlDate + " to " + workingDate + ".");
                        }
                        i++;
                    }
                } catch (NullPointerException e) {
                    System.out.println("ERROR: Date NOT Found in Stock csv.");
                }
            }
            i = 0;
            csvLine.close();
            n ++;
        }
        csvReader.close();
    }

    // Return percent growth or loss. Requires start date, end date, and ticker.
    public static Double stockPerformance(String start, String stop, Stock ticker) {
        return (ticker.close(stop) - ticker.close(start))/ticker.close(start);
    }

}

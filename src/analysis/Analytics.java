package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Analytics {
    public static void main(String[] args) {
        //TODO: For now stock history must be preloaded, need to fetch automatically from online API.
        comparePrediction("src/analysis/data/Futures.csv");
    }

    private static void comparePrediction(String path) {
        // Create scanner line object if file exists. Otherwise, die.
        Scanner csvReader = null;
        try {
            csvReader = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Cannot find futures csv.");
            e.printStackTrace();
        }

        // Contains tickers of stocks to be analyzed for a specific firm. Resets after every row to maintain order.
        // For example, if GS has TSLA then AVEO, and BOA has AVEO then TSLA, this ensures the objects in Hashmap can always be referenced in order.
        ArrayList<String> tempTickers = new ArrayList<>();

        // Contains ticker and its stock object.
        HashMap<String, Stock> tickers = new HashMap<>();

        //Initialize local variables.
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
                // Reset list of Tickers for every firm. Rebuild in correct order.
                tempTickers.clear();

                while (csvLine.hasNext()) {
                    String ticker = csvLine.next();
                    // If stock object doesn't already exist in Hashmap try creating new one from csv...
                    if(!tickers.containsKey(ticker)) {
                        try {
                            tickers.put(ticker, new StockImpl("src/analysis/data/" + ticker + ".csv"));
                            // Create stock objects and add to list.
                        } catch(NullPointerException e) {
                            System.out.println("ERROR: Cannot find Stock csv file.");
                        }
                    }
                    // If ticker doesn't exist in List, add.
                    if(!tempTickers.contains(ticker)) {
                        tempTickers.add(ticker);
                    }
                }
                n = 0;

                // If on 0% control row, set control date and move to next row.
            } else if (n == 1) {
                controlDate = identifier;

                // Otherwise, iterate through each cell and compare to predicted value.
            } else {
                try {
                    // While csv line has next cell, try to locate date and run analysis class...
                    while(csvLine.hasNext()) {
                        if (i == 0) { // If in first cell, capture date.
                            workingDate = identifier;
                        }
                        //TODO: Write function to find nearest date.

                        Double predicted = Double.parseDouble(csvLine.next());
                        Stock workingTickerasO = tickers.get(tempTickers.get(i));
                        String workingTickerasS = tempTickers.get(i);

                        //rudimentaryComparison(controlDate, workingDate, predicted, workingTicker);
                        DirectComparison(controlDate, workingDate, predicted, workingTickerasO, workingTickerasS);

                        i++; // increase counter for cell #.
                    }
                } catch (NullPointerException e) {
                    System.out.println("ERROR: Date NOT Found in Stock csv.");
                }
            }
            i = 0; // Reset counter for cell number.
            csvLine.close();
            n ++; // Increase counter for line number.
        }
        csvReader.close();
    }

    // Return percent growth or loss. Requires start date, end date, and ticker.
    private static Double stockPerformance(String start, String stop, Stock ticker) {
        return (ticker.close(stop) - ticker.close(start))/ticker.close(start);
    }

    private static void rudimentaryComparison(String controlDate, String workingDate, Double prediction, Stock ticker) {
        // If performance > predicted.
        if (stockPerformance(controlDate, workingDate, ticker) >= prediction) {
            System.out.println("Growth exceeds or meets prediction from " + controlDate + " to " + workingDate + ".");
        } else {
            System.out.println("Prediction exceeds growth from " + controlDate + " to " + workingDate + ".");
        }
    }

    private static void DirectComparison(String controlDate, String workingDate, Double prediction, Stock tickerO, String tickerS) {
        System.out.println(tickerS + " Actual: " + stockPerformance(controlDate, workingDate, tickerO));
        System.out.println(tickerS + " Predicted: " + prediction);
    }

}

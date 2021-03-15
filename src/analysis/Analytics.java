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
            e.printStackTrace();
        }

        // Initialize temporary ArrayList and local variables.
        ArrayList<Stock> tempTickers = new ArrayList<>();
        int n = 0; // Row number. Resets for each firm.
        int i = 0; // Cell number. Resets each row.
        String controlDate = null;
        String workingDate = null;

        while (csvReader.hasNextLine()) {
            // Create new scanner object to traverse each line.
            Scanner csvLine = new Scanner(csvReader.nextLine());
            csvLine.useDelimiter(",");

            // Define first cell of each line as identifier. Will either be firm abbreviation or date.
            String identifier = csvLine.next();

            // TODO: Determine why identifier length is different.
            //If identifier is firm abbreviation...
            if (identifier.length() == 3 || identifier.length() == 2) {
                System.out.println(identifier.length());
                while (csvLine.hasNext()) {
                    // TODO: Determine if object already exists.
                    // Create stock objects and add to list.
                    tempTickers.add(new StockImpl("src/analysis/data/" + csvLine.next() + ".csv"));
                }
                n = 0;

                // If on 0% control row, set control date and move to next row.
            } else if (n == 1) {
                controlDate = identifier;

                // Otherwise, iterate through each cell and compare to predicted value.
            } else {
                while(csvLine.hasNext()) {
                    if (i == 0) {
                        workingDate = identifier;
                    }
                    // If performance > predicted.
                    //TODO: Add error messages for missing file / date.
                    //TODO: Automatically find nearest date.
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
            n ++;
        }
        csvReader.close();
    }

    // Return percent growth or loss. Requires start date, end date, and ticker.
    public static Double stockPerformance(String start, String stop, Stock ticker) {
        return (ticker.close(stop) - ticker.close(start))/ticker.close(start);
    }

}

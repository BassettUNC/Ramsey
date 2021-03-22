package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class Analytics {
    public static void main(String[] args) {
        //TODO: For now stock history must be preloaded, need to fetch automatically from online API.
        relatePrediction("src/analysis/data/Futures.csv");
        dateConversion("2019-01-02");
    }

    private static void relatePrediction(String path) {
        // Create scanner line object if file exists. Otherwise, die.
        Scanner csvReader = null;
        try {
            csvReader = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Cannot find futures csv.");
            e.printStackTrace();
        }

        // Contains tickers of stocks to be analyzed for a specific firm. Resets after every row to maintain order.
        // For example, if GS has stockA before stockB, and BOA has StockB before StockA this ensures that...
        // ...objects in Hashmap can always be referenced in order without having to create new objects every iteration.
        ArrayList<String> tempTickers = new ArrayList<>();

        // Contains ticker and its stock object.
        HashMap<String, Stock> tickers = new HashMap<>();

        //Initialize local variables.
        Integer n = null; // Row number. Resets for each firm.
        int i = 0; // Cell number. Resets each row.
        String controlDate = null;
        String workingDate = null;
        String firm = null;

        // Begin Readings csv file.
        while (csvReader.hasNextLine()) {
            // Create new scanner object to traverse each line.
            Scanner csvLine = new Scanner(csvReader.nextLine());
            csvLine.useDelimiter(",");

            // Define first cell of each line as identifier. Will either be firm abbreviation or date.
            String identifier = csvLine.next();

            // If identifier is firm abbreviation...
            // Length of first cell is one longer than it should be... or operator accounts for this.
            if ((identifier.length() == 3 && n == null) || identifier.length() == 2) {
                // Reset list of Tickers for every firm. Rebuild in correct order.
                tempTickers.clear();
                firm = identifier;

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

                        Double predicted = Double.parseDouble(csvLine.next());
                        Stock tickerStock = tickers.get(tempTickers.get(i));
                        String tickerString = tempTickers.get(i);

                        directComparison(controlDate, workingDate, predicted, tickerStock, tickerString, firm);
                        // TODO: call comparison class once. store all data here in hashmap.

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

    // NOT CURRENTLY USED!
    // Convert date String to object of type Date.
    private static Date dateConversion(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (
                ParseException e) {
            System.out.println("ERROR: Cannot Parse Date");
            e.printStackTrace();
            return null;
        }
    }

    // Return percent growth or loss. Requires start date, end date, and ticker.
    private static Double stockPerformance(String start, String stop, Stock ticker) {
        return (ticker.close(stop) - ticker.close(start))/ticker.close(start);
    }

    private static void directComparison(String controlDate, String workingDate, Double prediction,
                                         Stock tickerO, String tickerS, String firm) {

        // Find difference between how the stock performed and how it was predicted to preform.
        Double performDifference = stockPerformance(controlDate, workingDate, tickerO) - prediction;

        // Define Variable to keep track of prediction's score.
        int score = 0;

        // Initialize List to keep track of prediction comments.
        ArrayList<String> comments = new ArrayList<>();

        // Initialize List to keep track of flags.
        ArrayList<String> flags = new ArrayList<>();

        // The following if block is used to set the base score based on performance difference.
        // For comments and flags, refer to ids file.
        if (performDifference >= 0) {
            comments.add("Prediction was " + Math.abs(performDifference) + "% lower than actual stock performance.");
            if (performDifference >= 0 && performDifference < .03) {
                score = 94;
                flags.add("Prediction was within 3% of stock performance.");
            } else if (performDifference >= .03 && performDifference < .05) {
                score = 90;
            } else if (performDifference >= .05 && performDifference < .1) {
                score = 88;
            } else if (performDifference >= .1 && performDifference < .15) {
                score = 85;
            } else if (performDifference >= .15 && performDifference < .2) {
                score = 80;
            } else if (performDifference >= .2 && performDifference < .25) {
                score = 75;
            } else if (performDifference >= .25 && performDifference < .3) {
                score = 70;
            } else if (performDifference >= .3 && performDifference < .35) {
                score = 70;
            } else if (performDifference >= .35 && performDifference < .4) {
                score = 65;
                flags.add("Prediction was over 30% lower than actual stock performance.");
            } else if (performDifference >= .4 && performDifference < .5) {
                score = 65;
                flags.add("Prediction was over 40% lower than actual stock performance.");
            } else if (performDifference >= .5) {
                flags.add("Prediction was over 50% lower than actual stock performance.");
                score = 60;
            }
        }
        if (performDifference < 0) {
            comments.add("Prediction was " + Math.abs(performDifference) + "% higher than actual stock performance.");
            if (performDifference < 0 && performDifference > -.03) {
                score = 92;
                flags.add("Prediction was within 3% of stock performance.");
            } else if (performDifference <= -.03 && performDifference > -.05) {
                score = 90;
            } else if (performDifference <= -.05 && performDifference > -.1) {
                score = 84;
            } else if (performDifference <= -.1 && performDifference > -.15) {
                score = 78;
            } else if (performDifference <= -.15 && performDifference > -.2) {
                score = 70;
            } else if (performDifference <= -.2 && performDifference > -.25) {
                score = 60;
            } else if (performDifference <= -.25 && performDifference > -.3) {
                score = 50;
            } else if (performDifference <= -.3 && performDifference > -.35) {
                score = 40;
            } else if (performDifference <= -.35 && performDifference > -.4) {
                score = 30;
                flags.add("Prediction was over 30% higher than actual stock performance.");
            } else if (performDifference <= .4 && performDifference > -.5) {
                score = 20;
                flags.add("Prediction was over 40% higher than actual stock performance.");
            } else if (performDifference <= -.5) {
                flags.add("Prediction was over 50% higher than actual stock performance.");
                score = 5;
            }
        }
        logScore(firm, tickerS, score, comments, flags);
    }

    private static void logScore (String firm, String ticker, int score, ArrayList<String> comments, ArrayList<String> flags) {
        System.out.println("firm: " + firm);
        System.out.println("ticker: " + ticker);
        System.out.println("score: " + score);
        //System.out.println("comments: " + comments.get(0));
        //System.out.println("flags: " + flags.get(0));
        System.out.println();
    }
}



package analysis;


import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Analytics {
    public static void main(String[] args) {
        completeComparison(parsePrediction("src/analysis/data/Forecasts.csv"));
    }


    /**
     * This method traverses a forecasts csv and passes
     * data to scoring methods in a systematic manner.
     * It reads each row individually and iterates through
     * the rows columns.
     * @param path Path to the forecast csv.
     * @return Arraylist of all results.
     */
    private static ArrayList<Result> parsePrediction(String path) {
        // Create scanner line object if file exists. Otherwise, die.
        Scanner csvReader = null;
        try {
            csvReader = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Cannot find futures csv.");
            e.printStackTrace();
        }

        /* Contains tickers of stocks to be analyzed for a specific firm. Resets after every row to maintain order.
        For example, if GS has stockA before stockB, and BOA has StockB before StockA this ensures that objects
        in Hashmap can always be referenced in order without having to create new objects every iteration. */
        ArrayList<String> tempTickers = new ArrayList<>();

        // Contains ticker and its stock object.
        HashMap<String, Stock> tickers = new HashMap<>();

        //Initialize local variables.
        Integer n = null; // Row number. Resets for each firm.
        int i = 0; // Cell number. Resets each row.
        String controlDate = null;
        String workingDate = null;
        String firm = null;

        ArrayList<Result> results = new ArrayList<>();


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

                        // Add results of directComparison to array.
                        results.add(directComparison(controlDate, workingDate,
                                predicted, tickerStock, tickerString, firm));
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
        return results;
    }


    /**
     * Convert date as string object to Date object
     * @param date Date as a string.
     * @return Date as a Date object.
     */
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


    /**
     * Return percent growth or loss for a stock over the start and stop dates.
     * @param start The start date for the comparison.
     * @param stop The end date for the comparison.
     * @param ticker The stock to retrieve performance metrics on.
     * @return Percent stock growth or loss.
     */
    private static Double stockPerformance(String start, String stop, Stock ticker) {
        return (ticker.close(stop) - ticker.close(start))/ticker.close(start);
    }


    /**
     * Compares stock prediction with historical stock performance
     * over a given time period.
     * @param controlDate The date the stock prediction was issued. The beginning.
     * @param workingDate The stop date. Where stock should be at predicted value.
     * @param prediction Stock's predicted growth or loss.
     * @param tickerO Ticker as stock object.
     * @param tickerS Ticker as string object.
     * @param firm Firm who made the prediction.
     * @return Type Result Contains: id (created from unix stamp), firm, ticker, score, date, comments, flags.
     */
    private static Result directComparison(String controlDate, String workingDate, Double prediction,
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
            comments.add("Prediction was " + Math.abs(performDifference) + "% lower than actual stock performance");
            if (performDifference >= 0 && performDifference < .03) {
                score = 94;
                flags.add("Prediction was within 3% of stock performance");
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
                flags.add("Prediction was over 30% lower than actual stock performance");
            } else if (performDifference >= .4 && performDifference < .5) {
                score = 65;
                flags.add("Prediction was over 40% lower than actual stock performance");
            } else if (performDifference >= .5) {
                flags.add("Prediction was over 50% lower than actual stock performance");
                score = 60;
            }
        }
        if (performDifference < 0) {
            comments.add("Prediction was " + Math.abs(performDifference) + "% higher than actual stock performance");
            if (performDifference < 0 && performDifference > -.03) {
                score = 92;
                flags.add("Prediction was within 3% of stock performance");
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
                flags.add("Prediction was over 30% higher than actual stock performance");
            } else if (performDifference <= .4 && performDifference > -.5) {
                score = 20;
                flags.add("Prediction was over 40% higher than actual stock performance");
            } else if (performDifference <= -.5) {
                flags.add("Prediction was over 50% higher than actual stock performance");
                score = 5;
            }
        }

        // Return type result
        return new Result(System.nanoTime(), firm, tickerS, score, workingDate, comments, flags, performDifference);
    }


    /**
     * Calls methods used to compare stock scores to one another.
     * Used to generate score modifiers.
     * @param results requires an arraylist of of objects type result.
     */
    public static void completeComparison(ArrayList<Result> results) {
        // If an investment firm goes against others and is correct: increase score.
        // If an investment firm goes against others and is incorrect: decrease score.
        // If all investment firms are incorrect: increase score.
        Double fvfINC = 1.1;
        Double fvfDEC = 0.8;
        firmVsFirm(results, fvfINC, fvfDEC);
        firmReputation(results, fvfINC, fvfDEC);
        // TODO: If a firm's past predictions on a stock are good: increase score.
        // TODO: If a firm's past predictions on a stock are bad: decrease score.
        // TODO: If a firm’s overall rating (reputation) is high: increase score.
        // TODO: If stock has been historically nonvolatile, and a firm’s prediction is bad: decrease score.
        // TODO: If stock has been historically volatile, and a firm’s prediction is good: increase score.

        //To print results.
        viewResult(results);

    }

    /**
     * Compares one firms performance on a specific stock to others.
     * If an investment firm goes against others and is correct: increase score.
     * If an investment firm goes against others and is incorrect: decrease score.
     * Also: increases score if stock all investment firms had <0 ccd performance scores.
     * @param results An array of type results.
     * @param incMod Modifier used to increase score when performance is better than most.
     * @param decMod Modifier used to decrease score performance is worse than most.
     */
    private static void firmVsFirm(ArrayList<Result> results, Double incMod, Double decMod) {
        ArrayList<String> workingTickers = new ArrayList<>();
        ArrayList<String> workingDates = new ArrayList<>();

        // Build list of tickers and included dates
        for (Result result : results) {
            if (!workingTickers.contains(result.ticker())) {
                workingTickers.add(result.ticker());
            }
            if (!workingDates.contains(result.date())) {
                workingDates.add(result.date());
            }
        }

        // CCD = Current Compare Date. Used to segregate all stock forecasts for a specific ticker on a specific day.
        // Initialize temp array to hold index values of all scores for particular ticker in results array.
        ArrayList<Integer> currentCompareTicker = new ArrayList<>();
        //Initialize temp TreeMap (must be sorted) to hold stock score, id pairs.
        TreeMap<Double, List<Long>> ccd = new TreeMap<>();
        // Initialize temp list to hold scores for particular ticker that share the same date in results array.
        List<Double> ccdList = new ArrayList<>();
        // Initialize temp list to hold Performance Results for particular ticker that share the same date in results array.
        List<Double> ccdListPD = new ArrayList<>();
        // Initialize temp list to hold IDs for particular ticker that share the same date in results array.
        List<Long> ccdListID = new ArrayList<>();

        // Traverse each ticker found in results array.
        for (String workingTicker : workingTickers) {
            // Create currentCompareTicker.
            for (int n = 0; n < results.size(); n++) {
                if (workingTicker.equals(results.get(n).ticker())) {
                    currentCompareTicker.add(n);
                }
            }

            // Traverse each ticker found in result array and create a set of dates for each ticker.
            // Each set of dates for each ticker is stored in ccd as (score, id)
            for (String workingDate : workingDates) {
                for (int n = 0; n < results.size(); n++) {
                    if (workingDate.equals(results.get(n).date()) && currentCompareTicker.contains(n)) {
                        ccdList.add((double) results.get(n).score());
                        ccdListPD.add(results.get(n).performDifference());
                        ccdListID.add(results.get(n).id());
                        if (ccd.containsKey((double) results.get(n).score())) {
                            List<Long> previousIds = new ArrayList<>(ccd.get((double) results.get(n).score()));
                            previousIds.add(results.get(n).id());
                            ccd.put((double) results.get(n).score(), previousIds);
                        } else {
                            List<Long> id = new ArrayList<>();
                            id.add(results.get(n).id());
                            ccd.put((double) results.get(n).score(), id);
                        }
                    }
                }

                // Create list of outliers and find median of scores in ccd.
                List<Double> outliers = new ArrayList<>(getOutliers(ccdList));
                Double ccdMedian = getMedian(ccdList);

                // Traverse list of outliers apply score modifier in results Array.
                for (Double outlier : outliers) {
                    int newScore;
                    String newFlag;
                    for (Result result : results) {
                        if (ccd.get(outlier).size() != 0 &&
                                result.id() == ccd.get(outlier).get(0)) {
                            if (result.score() >= ccdMedian) {
                                newScore = (int) (result.score() * incMod);
                                newFlag = "Prediction was significantly better than other firms";
                            } else {
                                newScore = (int) (result.score() * decMod);
                                newFlag = "Prediction was significantly worse than other firms";
                            }
                            // Set score
                            result.setScore(newScore);
                            // Add Flag
                            ArrayList<String> flags = new ArrayList<>(result.flags());
                            flags.add(newFlag);
                            result.setFlags(flags);
                        }
                        /* If all firms' stock performance's are sub-zero for a particular ticker on a particular
                         date increase score for all. */
                        if (allLoss(ccdListPD)) {
                            if (ccdListID.contains(result.id())) {
                                result.setScore((int) (result.score() * incMod));
                                ArrayList<String> flags = new ArrayList<>(result.flags());
                                flags.add("All firms did bad");
                                result.setFlags(flags);
                            }
                        }
                    }
                }

                ccdList.clear();
                ccd.clear();
                ccdListPD.clear();
                ccdListID.clear();
            }
            currentCompareTicker.clear();
        }
    }

    private static void firmReputation(ArrayList<Result> results, Double incMod, Double decMod) {
        HashMap<String, ArrayList<Long>> firmResults = new HashMap<>();

        // Create hashmap that contains keys of firms and values that are a list of a respective firm's result ids.
        for(Result result: results) {
            if (firmResults.containsKey(result.firm())) {
                ArrayList<Long> ids = firmResults.get(result.firm());
                ids.add(result.id());
                firmResults.replace(result.firm(), ids);
            } else {
                ArrayList<Long> ids = new ArrayList<>();
                ids.add(result.id());
                firmResults.put(result.firm(), ids);
            }
        }

        //TODO: Results arraylist needs to be an hashmap.

        // Find firms` average score and add to new hashmap.
        HashMap<String, Integer> averageScores = new HashMap<>();
        for(String firm: firmResults.keySet()) {
            ArrayList<Long> ids = firmResults.get(firm);
            int averageScore = 0;
            for (Long id: ids) {
                averageScore += getId(results, id).score();
            }
            if (averageScore / ids.size() > 70) {
                for (Long id: ids) {
                    getId(results, id).setScore((int) (getId(results, id).score() * incMod));
                }
            }
            averageScores.put(firm, averageScore / ids.size());
        }


        System.out.println(firmResults);
        System.out.println(averageScores);


    }

    /**
     * Helper method used to determine if all stock performance results are negative.
     * @param performanceData List of performanceData for a given set of stocks
     * @return true if all evaluations are negative.
     */
    private static boolean allLoss (List<Double> performanceData) {
        Double maxValue = -Double.MAX_VALUE;
        for (Double performanceDatum : performanceData) {
            if (performanceDatum > maxValue) { maxValue = performanceDatum; }
        }
        return maxValue < 0.0;
    }

    /**
     * This function does NOT produce statistically accurate results.
     * Upper and lower bounds are tighter to make more room for outliers in a presumably small data set.
     * If there become many financial firms involved, weight for bounds may need to be adjusted.
     * @param input unsorted values. MINIMUM OF 5 REQUIRED.
     * @return Any 'outliers' in a list.
     */
    public static List<Double> getOutliers(List<Double> input) {
        Collections.sort(input);
        List<Double> output = new ArrayList<>();
        List<Double> data1;
        List<Double> data2;
        if (input.size() % 2 == 0) {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2, input.size());
        } else {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2 + 1, input.size());
        }
        double q1 = getMedian(data1);
        double q3 = getMedian(data2);
        double iqr = q3 - q1;
        double lowerFence = q1 - .3 * iqr;
        double upperFence = q3 + .3 * iqr;
        for (Double aDouble : input) {
            if (aDouble <= lowerFence || aDouble >= upperFence)
                output.add(aDouble);
        }
        return output;
    }

    /**
     * Used to find median in a set of ordered values.
     * @param data An ordered List of Double values
     * @return Will return median as a double.
     */
    private static double getMedian(List<Double> data) {
        if (data.size() % 2 == 0)
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        else
            return data.get(data.size() / 2);
    }

    private static void viewResult (ArrayList<Result> results) {
        for (Result result : results) {
            System.out.println("id: " + result.id() + " | firm: " + result.firm() +
                    " | ticker: " + result.ticker() + " | score: " + result.score() +
                    " | date: " + result.date() + " | flags: " + result.flags());
        }

    }

    /**
     * This method is used for TESTING to view scores.
     * @param firm The firm who made the prediction for the score being logged.
     * @param ticker The ticker for which the prediction was made.
     * @param score The prediction score.
     * @param date The date that the prediction should be fulfilled.
     * @param comments Any comments regarding the prediction.
     * @param flags Any flags regarding the prediction.
     */
    private static void viewScore (String firm, String ticker, int score, String date,
                                   ArrayList<String> comments, ArrayList<String> flags) {
        System.out.println("firm: " + firm);
        System.out.println("ticker: " + ticker);
        System.out.println("score: " + score);
        System.out.println("comments: " + comments.get(0));
        System.out.println("flags: " + flags.get(0));
        System.out.println();
    }

    private static Result getId (ArrayList<Result> results, Long id) {
        for (Result result: results) {
            if (result.id() == id) {
                return result;
            }
        }
        return results.get(results.size() - 1);
    }
}
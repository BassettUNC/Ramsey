package analysis;

public class Panel {
    public static void main(String[] args) {
        // Create local variables
        String fromDate = "1099762684";
        String toDate = "1631298684";
        String[] tickers = new String []{"AVEO", "INFI", "TSLA"};

        for (String ticker : tickers) {
            Fetch.historicalRecord(ticker, fromDate, toDate);
        }

        //Run analytics on supplied forecasts file.
        Analytics.completeComparison(Analytics.parsePrediction("src/analysis/data/Forecasts.csv"));
    }

}

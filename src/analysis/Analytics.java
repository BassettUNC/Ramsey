package analysis;

public class Analytics {
    public static void main(String[] args) {
        Stock AVEO = new StockImpl("src/analysis/data/AVEO.csv");
        Stock TSLA = new StockImpl("src/analysis/data/TSLA.csv");
        System.out.println(TSLA.open("2019-02-01"));
    }

    public void comparePrediction() {
        //TODO: Determine Data structure for predictions
    }

    public void stockPerformance() {

    }

}

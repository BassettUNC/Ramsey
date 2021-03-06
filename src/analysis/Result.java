package analysis;

import java.util.ArrayList;

public class Result {

    private long _id;
    private String _firm;
    private String _ticker;
    private int _score;
    private String _date;
    private ArrayList<String> _comments;
    private ArrayList<String> _flags;
    private Double _performDifference;



    // Define constructor.
    public Result(long id, String firm, String ticker, int score, String date, ArrayList<String> comments,
                  ArrayList<String> flags, Double performDifference) {
        _id = id;
        _firm = firm;
        _ticker = ticker;
        _score = score;
        _date = date;
        _comments = comments;
        _flags = flags;
        _performDifference = performDifference;

    }

    public long id() {
        return _id;
    }

    public String firm() {
        return _firm;
    }

    public String ticker() { return _ticker; }

    public int score() { return _score; }

    public String date() {
        return _date;
    }

    public Double performDifference() { return _performDifference; }

    public ArrayList<String> comments() { return _comments; }

    public ArrayList<String> flags() { return _flags; }

    public void setScore(int score) { _score = score; }

    public void setFlags(ArrayList<String> flags) { _flags = flags; }


}

package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class StockImpl implements Stock{

    // Initialize local variable.
    private HashMap<String, ArrayList<Double>> _stock;

    // Initialize scanner. Must be done outside of constructor for some reason.
    Scanner csvReader = null;

    // Define constructor.
    public StockImpl(String path) {

        // Attempt to open file and initialize scanner object. If not found, throw exception.
        try {
            // Create scanner object.
            csvReader = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Define local variable _stock. Used to store csv.
        // Map organization: key is date. Value is an array that contains Open, High, Close, Volume.
        _stock = new HashMap<String, ArrayList<Double>>();

        // Initialize temporary ArrayList used to hold row values.
        ArrayList<Double> temp = new ArrayList<Double>();

        // Initialize variables for while: n used to add all row values to array. date holds date (key) for each iteration.
        int n = 0;
        String date = null;

        // Skip header row.
        csvReader.nextLine();

        // Traverse csv file and add contents to Hashtable.
        // Organization: Run csvReader to obtain each line, then run csvLine in each line to separate values.
        while(csvReader.hasNextLine()){

            // Create new scanner to traverse each line.
            Scanner csvLine = new Scanner(csvReader.nextLine());
            csvLine.useDelimiter(",");
            date = csvLine.next();

            // Iterate through entire line. Add all values to array.
            while(n<6) {
                temp.add(Double.parseDouble(csvLine.next()));
                n++;
            }

            //Add values to new array list.
            _stock.put(date, new ArrayList<Double>(temp));
            n = 0;
            temp.clear();
        }
        csvReader.close();
    }

    // Define class methods.
    public Double open (String date) { return _stock.get(date).get(0); }

    public Double high (String date) { return _stock.get(date).get(1); }

    public Double low (String date) { return _stock.get(date).get(2); }

    public Double close (String date) { return _stock.get(date).get(3); }

    public Double adjClose (String date) { return _stock.get(date).get(4); }

    public Double volume (String date) { return _stock.get(date).get(5); }

    public ArrayList<Double> all (String date) { return _stock.get(date); }

    public HashMap<String, ArrayList<Double>> map () { return _stock; }

}

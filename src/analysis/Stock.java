package analysis;

import java.util.ArrayList;
import java.util.HashMap;

public interface Stock {

    Double open (String date);
    Double high (String date);
    Double low (String date);
    Double close (String date);
    Double adjClose (String date);
    Double volume (String date);
    ArrayList<Double> all (String date);
    HashMap<String, ArrayList<Double>> map ();

}

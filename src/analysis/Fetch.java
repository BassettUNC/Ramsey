package analysis;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class Fetch {

        public static void historicalRecord(String ticker, String from, String to) {
            String url = "https://query1.finance.yahoo.com/v7/finance/download/" + ticker + "?period1=" + from + "&period2=" + to + "&interval=1d&events=history&includeAdjustedClose=true";
            try {
                downloadUsingStream(url, "src/analysis/data/" + ticker + ".csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void downloadUsingStream(String urlStr, String file) throws IOException{
            URL url = new URL(urlStr);
            BufferedInputStream input = new BufferedInputStream(url.openStream());
            FileOutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count=0;
            while((count = input.read(buffer,0,1024)) != -1)
            {
                output.write(buffer, 0, count);
            }
            output.close();
            input.close();
        }
}

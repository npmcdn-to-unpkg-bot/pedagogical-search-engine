package utils.javathings;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Files {
    public static InputStream openResource(String path) {
        InputStream is = File.class.getResourceAsStream(path);
        if(is == null) {
            System.out.println("Cannot open resource: " + path);
        }
        return is;
    }

    public static List<String> readLines(String path) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(openResource(path)));
        List<String> lines = new ArrayList<String>();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}

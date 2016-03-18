package utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class File2 {
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

    public static void write(String content, String path) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(path);
        pw.write(content);
        pw.close();
    }

    public static String read(String p) {
        return StringUtils.join(File2.readLines(p), "\n");
    }

    public static Properties loadProperties(String path) {
        try {
            Properties properties = new Properties();
            properties.load(openResource(path));

            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

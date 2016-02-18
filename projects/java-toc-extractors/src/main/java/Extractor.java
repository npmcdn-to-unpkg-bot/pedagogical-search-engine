import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tree.Node;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Extractor {
    public static final Logger logger = LoggerFactory.getLogger(Extractor.class);
    public static final String version = "v1.0";

    public static void main(String[] args) {
        // Get args
        String tocPath = null;
        String outputPath = null;
        if(args.length < 1) {
            logger.error("missing args: toc-path/ [output-path]");
            System.exit(1);
        }
        tocPath = args[0];
        if(args.length > 1) {
            outputPath = args[1]+(args[1].endsWith("/")? "": "/");
        } else {
            outputPath = "./";
        }
        outputPath+= "extracted-tocs-"+version;

        // Test args
        File inputFolder = new File(tocPath);
        if(!inputFolder.isDirectory()) {
            logger.error("input {} is not a folder", tocPath);
            System.exit(1);
        }
        File outputFolder = new File(outputPath);
        if(!outputFolder.exists()) {
            outputFolder.mkdir();
        }

        // Extract
        File[] inputFiles = inputFolder.listFiles();
        for(File inputFile: inputFiles) {
            String fn = inputFile.getName();
            try {
                String content = StringUtil.join(Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8), "");
                Document parsedDoc = Jsoup.parse(content);
                safaribooks.Extractor extractor = new safaribooks.Extractor(parsedDoc);

                // Extract Table Of Content
                Node toc = extractor.getToc();
                if(toc != null) {
                    File outputFile = new File(outputFolder.getAbsolutePath()+"/"+fn);
                    String relativeOutputFilePath = outputPath+"/"+fn;
                    try {
                        PrintWriter pw = new PrintWriter(outputFile);
                        pw.write(toc.toJSON().toString(1));
                        pw.close();
                        logger.info("Successfully extracted {}", relativeOutputFilePath);
                    } catch (Exception e) {
                        logger.error("Cannot write file {} , error: {}", relativeOutputFilePath, e.getMessage());
                        // but continue to next file
                    }
                } else {
                    logger.error("File as multiple tocs: {}", fn);
                    System.exit(1);
                }
            } catch (IOException e) {
                logger.error("Cannot read file {}, skipping it", fn);
                System.exit(1);
            }
        }
    }
}

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerController {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            logger.info("Needed parameters: ");
            logger.info("\t rootFolder (it will contain intermediate crawl data)");
            logger.info("\t numberOfCralwers (number of concurrent threads)");
            return;
        }

        Crawler.folderPath = args[0];
        int numberOfCrawlers = Integer.parseInt(args[1]);

        CrawlConfig config = new CrawlConfig();
        config.setResumableCrawling(true);
        config.setCrawlStorageFolder(Crawler.folderPath);

        int nbItems = 29819;
        int objectiveTime = 1*8*60*60*1000; // one night :-D (in ms)
        int ratio = 4; // 1 ToC each x(ratio) Links followed, static evaluation of the current crawler efficiency
        int desiredDelay = (objectiveTime*numberOfCrawlers)/(nbItems*ratio);
        int minPoliteTime = 200; // ms
        int maxPoliteTime = 2*1000; // ms
        int delay = Math.min(Math.max(minPoliteTime, desiredDelay), maxPoliteTime);
        logger.info("A delay of " + delay + "ms~ between each page load.. (we are polite)");
        config.setPolitenessDelay(delay);
        float expectedItemDelay = ((float)(delay*ratio))/1000; // sec
        logger.info("A delay of " + expectedItemDelay + "sec~ is expected between each ToC fetched");
        float expectedTime = (expectedItemDelay*nbItems)/(24*60*60*numberOfCrawlers); // days
        logger.info("" + expectedTime + " days is expected to fetch all ToCs");

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false); // don't respect robot.txt
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("http://proquest.safaribooksonline.com/search");
        controller.start(Crawler.class, numberOfCrawlers);
    }
}

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class Crawler extends WebCrawler {
    public static String folderPath = "./";

    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);

    private static final Pattern FILTERS = Pattern.compile(
            ".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf" +
                    "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    public static final Pattern bookPage = Pattern.compile("^http:\\/\\/proquest\\.safaribooksonline\\.com\\/(book)\\/(.+)\\?(bookview=overview)$");
    public static final Pattern tocPage = Pattern.compile("^http:\\/\\/proquest\\.safaribooksonline\\.com\\/(book)\\/(.+)\\?(bookview=toc)$");
    private static final Pattern searchPage = Pattern.compile("^http:\\/\\/proquest\\.safaribooksonline\\.com\\/(search)\\?(page=([0-9]+))(.+)$");

    CrawlStat myCrawlStat;

    public Crawler() {
        myCrawlStat = new CrawlStat();
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && (bookPage.matcher(href).matches()
                || tocPage.matcher(href).matches()
                || searchPage.matcher(href).matches());
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        logger.info("Visited: {}", url);
        myCrawlStat.incVisited();

        // We dump this crawler statistics after processing every 50 pages
        if ((myCrawlStat.getTotalVisited() % 50) == 0) {
            dumpMyData();
        }

        // Test if the page is a toc
        if(tocPage.matcher(url).matches()) {
            try {
                // Write page
                String encodedUrl = URLEncoder.encode(url, "UTF-8");
                String fn = encodedUrl;

                FileOutputStream fos = new FileOutputStream(new File(folderPath+"/"+fn));
                fos.write(page.getContentData());
                fos.close();
                logger.info("Saved: {}", url);
                myCrawlStat.incSaved();
            } catch (FileNotFoundException e) {
                logger.error("Unable to write " + url + ", FileNotFoundException: "+e.getMessage());
            } catch (UnsupportedEncodingException e) {
                logger.error("Unable to write " + url + ", UnsupportedEncodingException: " + e.getMessage());
            } catch (IOException e) {
                logger.error("Unable to write " + url + ", IOException: " + e.getMessage());
            }
        }
    }

    /**
     * This function is called by controller to get the local data of this crawler when job is finished
     */
    @Override
    public Object getMyLocalData() {
        return myCrawlStat;
    }

    /**
     * This function is called by controller before finishing the job.
     * You can put whatever stuff you need here.
     */
    @Override
    public void onBeforeExit() {
        dumpMyData();
    }

    public void dumpMyData() {
        int id = getMyId();
        int precision = 2;
        int factor = 10^precision;
        float niceRatio = ((float) Math.round(myCrawlStat.getRatio()*factor))/factor;
        // You can configure the log to output to file
        logger.info("Crawler {} > total Visited {}", id, myCrawlStat.getTotalVisited());
        logger.info("Crawler {} > Total Saved: {}", id, myCrawlStat.getTotalSaved());
        logger.info("Crawler {} > Ratio: {}", id, niceRatio);
    }
}

package htmlunit;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Stubborn {

    public final static long defaultWaitMs = 2000;
    private static final Logger logger = LoggerFactory.getLogger(Stubborn.class);

    public static <U> List<U> getByXPath(HtmlPage page, String path, int nbElements, int nbRetry, long sleepMs) {
        int attemptNo = 0;
        boolean success = false;
        List<U> results = new ArrayList<U>();

        while(attemptNo <= nbRetry && !success) {
            results = (List<U>) page.getByXPath(path);

            // test success
            if(results.size() == nbElements) {
                success = true;
            } else {
                if((nbRetry - attemptNo) >= 0) {
                    page.getWebClient().waitForBackgroundJavaScript(sleepMs);
                    logger.info(String.format("Retrying.. %d/%d",
                            attemptNo, nbRetry));
                }
            }

            // iterate
            attemptNo++;
        }
        return results;
    }

    public static <U> List<U> getByXPath(HtmlPage page, String path, int nbElements, int nbRetry) {
        return getByXPath(page, path, nbElements, nbRetry, defaultWaitMs);
    }

    public static <U> U getExactlyOneByXPath(HtmlPage page, String path, int nbRetry, long sleepMs) {
        final List<U> results = getByXPath(page, path, 1, nbRetry, sleepMs);

        if(results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public static <U> U getExactlyOneByXPath(HtmlPage page, String path, int nbRetry) {
        return getExactlyOneByXPath(page, path, nbRetry, defaultWaitMs);
    }
}

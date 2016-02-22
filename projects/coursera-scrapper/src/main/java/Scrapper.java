import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class Scrapper {
    public static void main(String[] args) {
        WebClient webClient = new WebClient();
        final HtmlPage page;
        try {
            page = webClient.getPage("http://htmlunit.sourceforge.net");
            String pageAsText = page.asText();

            System.out.println(pageAsText);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

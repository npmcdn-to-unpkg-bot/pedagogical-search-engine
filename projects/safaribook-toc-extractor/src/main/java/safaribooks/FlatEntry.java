package safaribooks;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlatEntry implements toc.FlatEntry<String> {
    public static final Logger logger = LoggerFactory.getLogger(FlatEntry.class);

    private String content = null;
    private int level = -1;

    public static final Pattern levelPattern = Pattern.compile("^level([0-9]+)$");

    public FlatEntry(Element entryEl) {
        Set<String> classes = entryEl.classNames();
        for(String c: classes) {
            Matcher m = levelPattern.matcher(c.trim());
            if(m.matches()) {
               level = Integer.valueOf(m.group(1));
            }
        }
        level+= 1;
        content = entryEl.text();
    }

    public int getLevel() {
        return level;
    }

    public String getContent() {
        return content;
    }
}

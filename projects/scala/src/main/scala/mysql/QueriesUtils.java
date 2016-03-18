package mysql;

import org.apache.commons.lang3.StringUtils;
import utils.Constants;
import utils.File2;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueriesUtils {

    public static String read(String queryFn) {
        // remove comments
        String query = join(trim(removeComments(rawRead(queryFn))));

        // replace keywords
        query = query.replace(Constants.Mysql.Keywords.database,
                Constants.Mysql.connection.getDatabase());

        return query;
    }

    public static String read(String queryFn, List<String> values) {
        return replace(read(queryFn), values);
    }

    /**
     * Will espace the values
     */
    public static String replace(String query, List<String> values) {
        for(int i = 0; i< values.size(); i++) {
            String var = "$_" + String.valueOf(i + 1);
            String value = values.get(i).replace("'", "\\'").replace(";", "");
            query = query.replace(var, value);
        }
        return query;
    }

    public static ResultSet execute(String query) {
        ResultSet rs = null;

        // Split query into (updates, deletes, insert) / select statements
        String[] statements = query.split(";");
        for(String statement: statements) {
            statement = statement.trim();
            if(statement.startsWith("SELECT")) {
                rs = Constants.Mysql.connection.query(statement);
            } else {
                Constants.Mysql.connection.update(statement);
            }
        }
        return rs;
    }

    public static String escapeAndJoin(Collection<String> xs) {
        if(xs.size() == 0) {
            return "NULL";
        } else {
            Collection<String> escaped = new ArrayList<String>();
            for(String x: xs) {
                escaped.add("\"" + x.replace("\"", "") + "\"");
            }
            return StringUtils.join(escaped, ",");
        }
    }

    private static String join(List<String> lines) {
        return StringUtils.join(lines, "\n");
    }

    private static List<String> rawRead(String queryFn) {
        return File2.readLines(String.format(
                "%s/%s",
                Constants.Paths.mysqlQueries,
                queryFn
        ));
    }

    private static List<String> trim(List<String> lines) {
        List<String> newLines = new ArrayList<String>();
        for(String line: lines) {
            newLines.add(line.trim());
        }
        return newLines;
    }

    private static String FilterHyphenHash(String line) {
        Pattern r_oneLineHashTag = Pattern.compile("^(.*)\\#.*$");
        Pattern r_oneLineHyphen = Pattern.compile("^(.*)\\-\\-.*$");

        Matcher m1 = r_oneLineHashTag.matcher(line);
        Matcher m2 = r_oneLineHyphen.matcher(line);
        if(m1.matches() && m2.matches()) {
            String prefix1 = m1.group(1);
            String prefix2 = m2.group(1);
            if(prefix1.length() < prefix2.length()) {
                return prefix1;
            } else {
                return prefix2;
            }
        } else if(m1.matches()) {
            return m1.group(1);
        } else if (m2.matches()) {
            return m2.group(1);
        } else {
            return line;
        }
    }

    /**
     * Parser built "by hand"
     */
    private static List<String> removeComments(List<String> lines) {
        // Build regexes
        Pattern r_multiLineOneLine = Pattern.compile("^(.*)\\/\\*.*\\*\\/(.*)$");
        Pattern r_multiLineEnd = Pattern.compile("^.*\\*\\/(.*)$");
        Pattern r_multiLineBegin = Pattern.compile("^(.*)\\/\\*.*$");
        Pattern r_whiteSpaces = Pattern.compile("^\\s*$");
        Pattern r_oneLineHashTag = Pattern.compile("^(.*)\\#.*$");
        Pattern r_oneLineHyphen = Pattern.compile("^(.*)\\-\\-.*$");

        // Filter out special case: inline multiline comments
        // i.e. /* I am a multiline on one line */
        boolean change;
        List<String> currentLines = lines;
        do  {
            // Init
            change = false;
            List<String> nextLines = new ArrayList<String>();

            // Process each line
            for(String line: lines) {
                Matcher m1 = r_multiLineOneLine.matcher(line);
                if(m1.matches()) {
                    change = true;
                    String prefix = m1.group(1);
                    String sufix = m1.group(2);
                    nextLines.add(prefix + sufix);
                } else {
                    nextLines.add(line);
                }
            }

            // prepare next iteration
            currentLines.clear();
            currentLines.addAll(nextLines);
        } while(change);

        // Process normal cases
        boolean multiLine = false;
        List<String> filteredLines = new ArrayList<String>();
        for(String line: currentLines) {
            if(multiLine) {
                // test if reach multiline end
                Matcher m1 = r_multiLineEnd.matcher(line);
                if(m1.matches()) {
                    multiLine = false; // end multiline
                    line = m1.group(1);
                }
            }

            if(!multiLine) { // not equal to an "else" clause
                // test if multline begin
                Matcher m1 = r_multiLineBegin.matcher(line);
                if(m1.matches()) {
                    multiLine = true; // begin multiline
                    String prefix = m1.group(1);
                    Matcher m2 = r_oneLineHashTag.matcher(prefix); // case: bla # .. /* ..
                    Matcher m3 = r_oneLineHyphen.matcher(prefix); // case: bla -- .. /* ..
                    if(m2.matches() || m3.matches()) {
                        multiLine = false; // abort multiline
                    } else {
                        Matcher m4 = r_whiteSpaces.matcher(prefix);
                        if(!m4.matches()) {
                            // Save the rest of the line
                            filteredLines.add(prefix);
                        }
                    }
                }

                if(!multiLine) {
                    // test if one line comment
                    String prefix = FilterHyphenHash(line);
                    Matcher m2 = r_whiteSpaces.matcher(prefix);
                    if(!m2.matches()) {
                        // Save the rest of the line
                        filteredLines.add(prefix);
                    }
                }
            }
        }
        return filteredLines;
    }
}

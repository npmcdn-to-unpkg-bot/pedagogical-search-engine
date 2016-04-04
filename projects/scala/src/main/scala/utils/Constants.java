package utils;

public class Constants {
    public static Settings settings = new Settings();
    public static Database database = new Database(settings);
    public static class Paths {
        public static String mysqlQueries = "/mysql-queries";
    }
    public static class Graph {
        public static class Edges {
            public static class Attribute {
                public static String completeWlm = "CWLM";
                public static String normalizedCwlm = "NormCMWL";
            }
        }
    }
    public static class Mysql {
        public static class QueriesPath {
            public static String queryOutLinks = "query-out-links.sql";
            public static String queryOutLinksRestricted = "query-out-links-restricted.sql";
        }
        public static class Keywords {
            public static String database = "$_DATABASE";
        }
    }
}

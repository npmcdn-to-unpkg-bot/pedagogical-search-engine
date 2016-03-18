package utils;

import mysql.Connection;


public class Constants {
    public static Settings settings = new Settings();
    public static Amadeus amadeus = new Amadeus();
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
        public static Connection connection = amadeus.getConnection(settings);
        public static class QueriesPath {
            public static String queryOutLinks = "query-out-links.sql";
        }
        public static class Keywords {
            public static String database = "$_DATABASE";
        }
    }
}

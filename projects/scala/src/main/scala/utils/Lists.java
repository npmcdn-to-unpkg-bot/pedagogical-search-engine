package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Lists {
    public static int[] toArrayInt(List<Integer> xs) {
        int[] array = new int[xs.size()];
        for (int i = 0; i < xs.size(); i++) array[i] = xs.get(i);
        return array;
    }
    public static double[] toArrayDouble(List<Double> xs) {
        double[] array = new double[xs.size()];
        for (int i = 0; i < xs.size(); i++) array[i] = xs.get(i);
        return array;
    }
    public static double[] toArrayDoubleFromInt(List<Integer> xs) {
        double[] array = new double[xs.size()];
        for (int i = 0; i < xs.size(); i++) array[i] = (double) xs.get(i);
        return array;
    }
    public static Double[] toArrayDoubleO(List<Double> xs) {
        Double[] array = new Double[xs.size()];
        for (int i = 0; i < xs.size(); i++) array[i] = xs.get(i);
        return array;
    }
    public static <U> List<U> initialize(int n, U value) {
        List<U> xs = new ArrayList<U>();
        for(int i = 0; i < n; i++) {
            xs.add(value);
        }
        return xs;
    }
    public static List<Double> divideBy(List<Double> xs, int n) {
        if(n == 0) {
            return xs;
        } else {
            List<Double> newXs = new ArrayList<Double>();
            for(Double x: xs) {
                newXs.add(x / (double) n);
            }
            return newXs;
        }
    }
    public static <U> boolean containsOne(Collection<U> xs, Collection<U> ys) {
        boolean contains = false;
        for(U x: xs) {
            if(ys.contains(x)) {
                contains = true;
            }
        }
        return contains;
    }
}

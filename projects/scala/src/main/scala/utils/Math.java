package utils;

import java.util.*;

public class Math {
    public static Double sumD(Collection<Double> xs) {
        Double sum = 0.d;
        for(Double x: xs) {
            sum += x;
        }
        return sum;
    }

    public static <U> int intersectSize(Set<U> A, Set<U> B) {
        int sizeA = A.size();
        int sizeB = B.size();
        Set<U> Union = new TreeSet<U>();
        Union.addAll(A);
        Union.addAll(B);
        int sizeUnion = Union.size();
        return sizeA + sizeB - sizeUnion;
    }

    public static double progress(long total, long current) {
        return (double) current / (double) total;
    }

    public static long stepSize(long total, int magnitude) {
        return java.lang.Math.max(1, total / (long) magnitude);
    }

    public static List<Double> normalize(List<Double> xs) {
        double sum = sumD(xs);
        if(sum > 0) {
            List<Double> ys = new ArrayList<Double>();
            for(Double x: xs) {
                ys.add(x / sum);
            }
            return ys;
        } else {
            return xs;
        }
    }
}

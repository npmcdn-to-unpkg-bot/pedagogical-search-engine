package wlm;

import java.util.*;

public class Compute {
    public static final long W = (long) Double.MAX_VALUE; // less than number of wikipedia articles (2015.10)

    /**
     *  score = log( common / max ) / log( W / min )
     *
     * Notes:
     *  log( W / min ) > 0
     *  min increases -> log(.) decreases -> final score gets worse
     *
     *  <--- increasing min <----
     *  -1/1 < -1/2 < -1/4 < -1/8 < .. < 0
     *  <--- decreasing log <----
     *  <--- score iet worse <----
     *
     *  i.e. we want a small minimum
     *
     *  log( common / max ) < 0
     *  common increases -> log(.) increases (towards 0) -> final score is better
     *  max increases -> log(.) decreases (far from 0) -> final score gets worse
     *
     */
    public static Double relatedness(Set A, Set B) {
        // Google distance
        double weight;
        int max = Math.max(A.size(), B.size());
        int min = Math.min(A.size(), B.size());
        int common = utils.Math.intersectSize(A, B);

        // max=0 -> both have no links..!
        // min=0 -> at least one has no links..!
        // common=0 -> no common links..!
        if(max == 0 || min == 0 || common == 0) {
            // a "worst case" situation (arbitrary)
            common = 1;
            max = 2*100;
            min = max;
        }
        weight = Math.log((double) common / (double) max) / Math.log((double) W / (double) min);
        return weight;
    }

    public static Double relatedness(Collection A, Collection B) {
        return relatedness(new TreeSet(A), new TreeSet(B));
    }
}

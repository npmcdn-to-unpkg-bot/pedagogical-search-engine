
public class CrawlStat {
    private long totalVisited;
    private long totalSaved;

    public long getTotalSaved() {
        return totalSaved;
    }

    public long getTotalVisited() {
        return totalVisited;
    }

    public void incSaved() { totalSaved++; }
    public void incVisited() { totalVisited++; }

    public float getRatio() {
        if(totalSaved == 0) {
            return (float) -1;
        } else {
            return ((float) totalVisited)/totalSaved;
        }
    }
}

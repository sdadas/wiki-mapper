package pl.sdadas.wikimapper.model;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sławomir Dadas
 */
public class PagesStats {

    private int allArticles;

    private int labelledArticles;

    private Map<String, MutableInt> labels = new HashMap<>();

    private boolean refreshing;

    public PagesStats(int allArticles) {
        this.allArticles = allArticles;
    }

    public int getLabelledArticles() {
        return labelledArticles;
    }

    public void setLabelledArticles(int labelledArticles) {
        this.labelledArticles = labelledArticles;
    }

    public int getAllArticles() {
        return allArticles;
    }

    public void setAllArticles(int allArticles) {
        this.allArticles = allArticles;
    }

    public int getLabelledPercent() {
        if(allArticles == 0) return 0;
        else return Math.round(((float) labelledArticles * 100.0f) / (float)allArticles);
    }

    public Map<String, MutableInt> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, MutableInt> labels) {
        this.labels = labels;
    }

    public void addLabel(String label) {
        MutableInt counter = labels.get(label);
        if(counter == null) {
            counter = new MutableInt(0);
            labels.put(label, counter);
        }
        counter.increment();
        labelledArticles++;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }
}

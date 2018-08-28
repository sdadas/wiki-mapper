package pl.sdadas.wikimapper.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sławomir Dadas
 */
public class Category extends Page {

    private Set<Page> children = new HashSet<>();

    private int childArticles;

    private int labelledArticles;

    private Long lock;

    public Category(long id, String name) {
        super(id, name);
    }

    public Set<Page> getChildren() {
        return children;
    }

    public void setChildren(Set<Page> children) {
        this.children = children;
    }

    @Override
    public int getChildArticles() {
        return childArticles;
    }

    public void setChildArticles(int childArticles) {
        this.childArticles = childArticles;
    }

    @Override
    public int getLabelledArticles() {
        return labelledArticles;
    }

    @Override
    public int getUnlabelledArticles() {
        return childArticles - labelledArticles;
    }

    @Override
    public float getLabelledPercent() {
        return (float)labelledArticles / (float)childArticles;
    }

    public void setLabelledArticles(int labelledArticles) {
        this.labelledArticles = labelledArticles;
    }

    public Stream<Page> childArticles() {
        return this.children.stream().filter(val -> val.getClass().equals(Page.class));
    }

    public Stream<Category> childCategories() {
        return this.children.stream().filter(val -> val.getClass().equals(Category.class)).map(val -> (Category) val);
    }

    @Override
    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    @Override
    public boolean hasChildCategories() {
        return childCategories().findAny().isPresent();
    }

    void addChild(Page child) {
        children.add(child);
    }

    @Override
    public void clearStats() {
        super.clearStats();
        this.childArticles = 0;
        this.labelledArticles = 0;
        this.lock = null;
    }

    void incrementStats(boolean labelled, long lockId) {
        if(lock != null && lock.equals(lockId)) return;
        this.lock = lockId;
        this.childArticles++;
        if(labelled) this.labelledArticles++;
        getParents().forEach(parent -> parent.incrementStats(labelled, lockId));
    }
}

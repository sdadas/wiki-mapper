package pl.sdadas.wikimapper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class FieldIndex {

    private List<Page> articles;

    private List<Category> categories;

    private Comparator<Page> comparator;

    public FieldIndex(Collection<Page> articles, Collection<Category> categories, Comparator<Page> comparator) {
        this.comparator = comparator;
        this.articles = new ArrayList<>(articles);
        this.categories = new ArrayList<>(categories);
    }

    public void sort() {
        categories.sort(this.comparator);
        articles.sort(this.comparator);
    }

    public List<Page> articles() {
        return this.articles;
    }

    public List<Category> categories() {
        return this.categories;
    }
}

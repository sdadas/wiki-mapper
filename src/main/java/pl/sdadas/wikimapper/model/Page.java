package pl.sdadas.wikimapper.model;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sławomir Dadas
 */
public class Page implements Serializable, Comparable<Page> {

    private long id;

    private String name;

    private String label;

    private String inheritedLabel;

    private int inheritedLabelDepth;

    private boolean inheritedLabelInitialized;

    private Set<Category> parents = new HashSet<>();

    public Page(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getNameNormalized() {
        return StringUtils.replace(name, " ", "_");
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Category> getParents() {
        return parents;
    }

    public void setParents(Set<Category> parents) {
        this.parents = parents;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getInheritedLabel() {
        return inheritedLabel;
    }

    public void setInheritedLabel(String inheritedLabel) {
        this.inheritedLabel = inheritedLabel;
    }

    public int getChildArticles() {
        return 0;
    }

    public int getLabelledArticles() {
        return getAssignedLabel() != null ? 1 : 0;
    }

    public int getUnlabelledArticles() {
        return getAssignedLabel() != null ? 0 : 1;
    }

    public float getLabelledPercent() {
        return getAssignedLabel() != null ? 1 : 0;
    }
    
    public void addParent(Category parent) {
        parents.add(parent);
        parent.addChild(this);
    }

    public boolean hasChildren() {
        return false;
    }

    public boolean hasChildCategories() {
        return false;
    }

    public boolean hasParentCategories() {
        return !getParents().isEmpty();
    }

    public void clearStats() {
        this.inheritedLabel = null;
        this.inheritedLabelDepth = 0;
        this.inheritedLabelInitialized = false;
    }

    public void refreshStats() {
        computeInheritedLabel();
        boolean labelled = label != null || inheritedLabel != null;
        getParents().forEach(parent -> parent.incrementStats(labelled, id));
    }

    public void computeInheritedLabel() {
        this.computeInheritedLabel(0);
    }

    Label computeInheritedLabel(int depth) {
        if(depth > 30) return null;
        if(inheritedLabelInitialized) {
            return getInheritedLabel() != null ? new Label(getInheritedLabel(), inheritedLabelDepth + depth) : null;
        }

        Label result = null;
        if(getLabel() != null) {
            this.setInheritedLabel(getLabel());
            this.inheritedLabelDepth = 0;
            result = new Label(getLabel(), depth);
        }

        List<Label> labels = getParents().stream()
                .map(parent -> parent.computeInheritedLabel(depth + 1))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(o -> o.depth))
                .collect(Collectors.toList());
        if(labels.size() > 0 && this.getInheritedLabel() == null) {
            Label label = findMatchingLabel(labels);
            this.setInheritedLabel(label.value);
            this.inheritedLabelDepth = label.depth - depth;
            result = label;
        }
        this.inheritedLabelInitialized = true;
        return result;
    }

    private Label findMatchingLabel(List<Label> labels) {
        int minDepth = labels.get(0).depth;
        List<String> minDepthLabels = labels.stream()
                .filter(label -> label.depth == minDepth)
                .map(label -> label.value).collect(Collectors.toList());
        if(minDepthLabels.size() == 1) {
            return new Label(minDepthLabels.get(0), minDepth);
        } else {
            Multiset<String> labelSet = HashMultiset.create();
            labelSet.addAll(minDepthLabels);
            String result = null;
            int count = 0;
            for (Multiset.Entry<String> entry: labelSet.entrySet()) {
                if(entry.getCount() > count) {
                    count = entry.getCount();
                    result = entry.getElement();
                }
            }
            return new Label(result, minDepth);
        }
    }

    public String getAssignedLabel() {
        return ObjectUtils.firstNonNull(this.getLabel(), this.getInheritedLabel());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof Page) {
            Page page = (Page) other;
            return new EqualsBuilder()
                    .append(id, page.id)
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Page other) {
        return Integer.compare(other.getChildArticles(), this.getChildArticles());
    }

    class Label {
        private String value;
        private int depth;

        Label(String value, int depth) {
            this.value = value;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return String.format("{%s, %d}", value, depth);
        }
    }
}

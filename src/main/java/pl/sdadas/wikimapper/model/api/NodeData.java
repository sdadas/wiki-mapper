package pl.sdadas.wikimapper.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeData {

    private String label;

    private String inheritedLabel;

    private int childArticles;

    private int labelledArticles;

    private Boolean childrenGroup;

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
        return childArticles;
    }

    public void setChildArticles(int childArticles) {
        this.childArticles = childArticles;
    }

    public int getLabelledArticles() {
        return labelledArticles;
    }

    public void setLabelledArticles(int labelledArticles) {
        this.labelledArticles = labelledArticles;
    }

    public Boolean getChildrenGroup() {
        return childrenGroup;
    }

    public void setChildrenGroup(Boolean childrenGroup) {
        this.childrenGroup = childrenGroup;
    }
}

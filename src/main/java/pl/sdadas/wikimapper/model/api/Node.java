package pl.sdadas.wikimapper.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import pl.sdadas.wikimapper.model.Category;
import pl.sdadas.wikimapper.model.ChildrenGroupPage;
import pl.sdadas.wikimapper.model.Page;

import java.util.List;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node {

    private String title;

    private String key;

    private boolean folder = true;

    private boolean lazy;

    private List<Node> children;

    private NodeData data = new NodeData();

    public Node(String title, String key) {
        this.title = title;
        this.key = key;
    }

    public <E extends Page> Node(E value, ExpandType expansion) {
        this.title = value.getName();
        this.key = String.valueOf(value.getId());
        this.lazy = expansion.equals(ExpandType.CHILDREN) ? value.hasChildren() : value.hasParentCategories();
        this.data.setLabel(value.getLabel());
        this.data.setInheritedLabel(value.getInheritedLabel());
        if(value instanceof Category) {
            this.data.setChildArticles(value.getChildArticles());
            this.data.setLabelledArticles(((Category) value).getLabelledArticles());
        } else if(value instanceof ChildrenGroupPage) {
            this.folder = false;
            this.key = ((ChildrenGroupPage) value).getVirtualId();
            this.data.setChildrenGroup(true);
        } else {
            this.folder = false;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public NodeData getData() {
        return data;
    }

    public void setData(NodeData data) {
        this.data = data;
    }
}

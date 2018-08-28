package pl.sdadas.wikimapper.model;

/**
 * @author Sławomir Dadas
 */
public class ChildrenGroupPage extends Page {

    public ChildrenGroupPage(long id, String name) {
        super(id, name);
    }

    public String getVirtualId() {
        return "children:" + this.getId();
    }
}

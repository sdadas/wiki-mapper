package pl.sdadas.wikimapper.model.api;

import java.util.List;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
public class LabelRequest {

    private List<String> ids;

    private String label;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

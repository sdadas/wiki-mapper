package pl.sdadas.wikimapper.model.api;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
public class SearchRequest {

    private String search;

    private ExpandType expand;

    private ShowType show;

    private Integer limit;

    private Integer offset;

    private String sort;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public ExpandType getExpand() {
        return expand;
    }

    public void setExpand(ExpandType expand) {
        this.expand = expand;
    }

    public ShowType getShow() {
        return show;
    }

    public void setShow(ShowType show) {
        this.show = show;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}

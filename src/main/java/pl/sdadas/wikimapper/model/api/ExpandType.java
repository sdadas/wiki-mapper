package pl.sdadas.wikimapper.model.api;

import pl.sdadas.wikimapper.model.Category;
import pl.sdadas.wikimapper.model.ChildrenGroupPage;
import pl.sdadas.wikimapper.model.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
public enum ExpandType {

    CHILDREN {
        @Override
        public List<Page> expand(Page value) {
            List<Page> result = ((Category) value).childCategories().sorted().collect(Collectors.toList());
            long directChildArticles = ((Category) value).childArticles().count();
            if(directChildArticles > 0) {
                String name = String.format("[%s direct child articles]", directChildArticles);
                Page articles = new ChildrenGroupPage(value.getId(), name);
                result.add(articles);
            }
            return result;
        }
    },

    CATEGORY_PARENTS {
        @Override
        public List<Page> expand(Page value) {
            List<Page> res = new ArrayList<>(value.getParents());
            Collections.sort(res);
            return res;
        }
    },

    ARTICLE_PARENTS {
        @Override
        public List<Page> expand(Page value) {
            List<Page> res = new ArrayList<>(value.getParents());
            Collections.sort(res);
            return res;
        }
    };

    public abstract List<Page> expand(Page value);
}

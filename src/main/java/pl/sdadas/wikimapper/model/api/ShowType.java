package pl.sdadas.wikimapper.model.api;

import pl.sdadas.wikimapper.model.Page;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
public enum ShowType {

    ALL {
        @Override
        public boolean matches(Page page) {
            return true;
        }
    },

    LABELLED {
        @Override
        public boolean matches(Page page) {
            return page.getLabel() != null || page.getInheritedLabel() != null;
        }
    },

    UNLABELLED {
        @Override
        public boolean matches(Page page) {
            return page.getLabel() == null && page.getInheritedLabel() == null;
        }
    };

    public abstract boolean matches(Page page);
}

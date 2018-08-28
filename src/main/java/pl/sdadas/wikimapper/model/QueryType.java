package pl.sdadas.wikimapper.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.BiPredicate;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author Sławomir Dadas
 */
public enum QueryType {

    STARTS_WITH((query, val) -> startsWithIgnoreCase(val.getName(), query)),
    ENDS_WITH((query, val) -> endsWith(val.getName(), query)),
    EQUALS((query, val) -> equalsIgnoreCase(val.getName(), query)),
    CONTAINS((query, val) -> containsIgnoreCase(val.getName(), query)),
    NUMERIC((query, val) -> contains(val.getName(), query) || Objects.equals(val.getId(), Long.valueOf(query)));

    private final BiPredicate<String, Page> predicate;

    QueryType(BiPredicate<String, Page> predicate) {
        this.predicate = predicate;
    }

    public boolean matches(String query, Page page) {
        return this.predicate.test(query, page);
    }

    public static QueryType queryType(String query) {
        if(startsWith(query, "^") && endsWith(query, "$")) {
            return EQUALS;
        } else if(startsWith(query, "^")) {
            return STARTS_WITH;
        } else if(endsWith(query, "$")) {
            return ENDS_WITH;
        } else if(StringUtils.isNumeric(query)) {
            return NUMERIC;
        } else {
            return CONTAINS;
        }
    }
}

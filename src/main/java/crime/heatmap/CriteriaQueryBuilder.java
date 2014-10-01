package crime.heatmap;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: victor
 * Date: Dec 7, 2010
 */
class CriteriaQueryBuilder {

    private StringBuilder query;
    private List<Object> values = new ArrayList<>();
    private boolean hasWhere;
    private boolean hasOrderBy = false;
    private boolean hasLimit = false;

    public CriteriaQueryBuilder(String selectAndFromClause) {
        query = new StringBuilder(selectAndFromClause);
    }

    public CriteriaQueryBuilder whereEquals(String param, Object value) {
        if (value == null) {
            return this;
        }
        checkAppendWhere();
        query.append(param).append("=? and ");
        values.add(value);
        return this;
    }

    public CriteriaQueryBuilder whereNotEquals(String param, Object value) {
        if (value == null) {
            return this;
        }
        checkAppendWhere();
        query.append(param).append("<>? and ");
        values.add(value);
        return this;
    }

    public CriteriaQueryBuilder whereGreaterOrEquals(String param, Object value) {
        if (value == null) {
            return this;
        }
        checkAppendWhere();
        query.append(param).append(">=? and ");
        values.add(value);
        return this;
    }

    public CriteriaQueryBuilder whereOrEquals(String param, Object value) {
        if (value == null) {
            return this;
        }
        if (!hasWhere) {
            query.append(" where ");
            query.append(param).append("=? or ");
            hasWhere = true;
        } else {
            for (int i = 4; i > 0; i--) {
                query.deleteCharAt(query.length() - i);
            }
            query.append("or ");
            query.append(param).append("=? and ");
        }
        values.add(value);
        return this;
    }

    public CriteriaQueryBuilder whereLike(String param, String value) {
        if (!StringUtils.hasText(value)) {
            return this;
        }
        checkAppendWhere();
        query.append(param).append(" like ? and ");
        values.add("%" + value + "%");
        return this;
    }

    public CriteriaQueryBuilder whereBetween(String param, Object from, Object to) {
        if (from == null || to == null) {
            return this;
        }
        checkAppendWhere();
        query.append(param).append(" between ? and ? and ");
        values.add(from);
        values.add(to);
        return this;
    }

    public CriteriaQueryBuilder whereIn(String param, Object... values) {
        if (values == null || values.length < 1) {
            return this;
        }
        checkAppendWhere();
        query.append(param).append(" in ( ");
        for (Object value : values) {
            this.query.append("?,");
            this.values.add(value);
        }
        //delete last comma
        query.deleteCharAt(query.length() - 1);
        query.append(" ) and ");
        return this;
    }

    public CriteriaQueryBuilder whereLikePrePost(String param, String value, boolean pre, boolean post) {
        if (!StringUtils.hasText(value)) {
            return this;
        }
        checkAppendWhere();
        query.append(param).append(" like ? and ");
        StringBuilder appendedValue = new StringBuilder();
        if (pre) {
            appendedValue.append("%");
        }
        appendedValue.append(value);
        if (post) {
            appendedValue.append("%");
        }
        values.add(appendedValue.toString());
        return this;
    }

    public CriteriaQueryBuilder whereClause(String clause, Object... params) {
        if (!StringUtils.hasText(clause)) {
            return this;
        }
        checkAppendWhere();
        query.append(clause).append(" and ");
        this.values.addAll(Arrays.asList(params));
        return this;
    }


    public CriteriaQueryBuilder limit(Integer end) {
        if (end == null) {
            return this;
        }
        checkAppendLimit();
        values.add(end);
        return this;
    }

    public CriteriaQueryBuilder orderBy(String param, boolean descending) {
        if (param == null) {
            return this;
        }
        checkAppendOrderBy();
        query.append(param);
        if (descending) {
            query.append(" desc");
        }
        return this;
    }

    public String sql() {
        if (hasWhere && !hasLimit && !hasOrderBy) {
            return query.toString().substring(0, query.toString().lastIndexOf("and"));
        } else {
            return query.toString();
        }
    }

    public List<Object> values() {
        return values;
    }

    private void checkAppendWhere() {
        if (!hasWhere) {
            query.append(" where ");
            hasWhere = true;
        }
    }

    private void checkAppendOrderBy() {
        if (!hasOrderBy) {
            if (hasWhere) {
                query.delete(query.length() - 4, query.length());
            }
            query.append(" order by ");
            hasOrderBy = true;
        } else {
            query.append(", ");
        }
    }

    private void checkAppendLimit() {
        if (!hasOrderBy && hasWhere) {
            query.delete(query.length() - 4, query.length());
        }
        query.append(" limit ?");
        hasLimit = true;
    }

}
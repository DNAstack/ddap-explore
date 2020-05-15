package com.dnastack.ddap.explore.apps.search.model;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;

@Data
public class SimpleSearchRequest {


    Map<String, SearchFilter> filters;
    List<OrderByFilter> order;


    @Data
    public static class SearchFilter {

        private FilterOperation operation;
        private Object value;

        public String getFilterString(){
            Object o =value;

            if (operation.equals(FilterOperation.NOT_NULL) || operation.equals(FilterOperation.NULL)){
                return operation.getOp();
            }

            if (operation.equals(FilterOperation.LIKE)){
                o = "%" + o.toString() + "%";
            }

            if (o instanceof String){
                o = "'" + ((String) o).replaceAll("'","\\'") + "'";
            }

            return operation.getOp() + " " + o.toString();
        }
    }

    @Data
    public static class OrderByFilter {

        String field;
        OrderBy direction;

    }

    public enum FilterOperation {
        LIKE("LIKE"), GT(">"), GTE(">="), LT("<"), LTE("<="), NEQ("!="), EQ("="),NOT_NULL("IS NOT NULL"),NULL("IS NULL");

        @Getter
        private String op;

        FilterOperation(String op) {
            this.op = op;
        }
    }

    public enum  OrderBy {
        DESC, ASC
    }


}

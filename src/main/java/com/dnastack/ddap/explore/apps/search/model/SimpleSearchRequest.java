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
            Object o = value;
            if (o instanceof String){
                o = "'" + o + "'";
            }

            switch (operation){
                case NOT_NULL:
                    return operation.getOp();
                case NULL:
                    return operation.getOp();
                default:
                    return operation.getOp() + " " + o.toString();
            }
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

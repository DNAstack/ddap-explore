package com.dnastack.ddap.explore.apps.dataset.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TableData {

    List<Map<String, Object>> data;
    Map<String, String> pagination;

    @JsonProperty("data_model")
    Map<String, Object> dataModel;

}

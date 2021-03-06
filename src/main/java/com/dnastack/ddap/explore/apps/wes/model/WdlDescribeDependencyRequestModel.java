package com.dnastack.ddap.explore.apps.wes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WdlDescribeDependencyRequestModel {

    private String uri;
    private String content;

}

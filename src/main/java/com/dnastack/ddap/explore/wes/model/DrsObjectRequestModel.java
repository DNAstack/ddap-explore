package com.dnastack.ddap.explore.wes.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DrsObjectRequestModel {

    private DrsObjectModel object;

}

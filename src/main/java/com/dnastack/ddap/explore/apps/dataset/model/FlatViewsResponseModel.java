package com.dnastack.ddap.explore.apps.dataset.model;

import lombok.Data;

import java.util.Map;

@Data
public class FlatViewsResponseModel {

    Map<String, FlatView> views;

}

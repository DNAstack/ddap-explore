package com.dnastack.ddap.explore.resource.model;

import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class PaginatedResponse<T> {

    @NonNull
    private List<T> data;
    private String nextPageToken;
}

package com.dnastack.ddap.explore.resource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DamId extends Id {

    private static final long serialVersionUID = 5610629031925135439L;

    public DamId() {
        additionalProperties = new HashMap<>();
    }

    public DamId(Id id) {
        super(id);
    }

    @JsonIgnore
    public String getViewName() {
        return additionalProperties.get("v");
    }

    @JsonIgnore
    public String getRoleName() {
        return additionalProperties.get("ro");
    }

    @JsonIgnore
    public String getInterfaceType() {
        return additionalProperties.get("i");
    }

    @JsonIgnore
    public void setViewName(String view) {
        additionalProperties.put("v", view);
    }

    @JsonIgnore
    public void setRoleName(String role) {
        additionalProperties.put("ro", role);
    }

    @JsonIgnore
    public void setInterfaceType(String interfaceType) {
        additionalProperties.put("i", interfaceType);
    }

    public String toFlatViewPrefix() {
        return String.format("/%s/%s/%s", getCollectionId(), getViewName(), getRoleName());
    }
}

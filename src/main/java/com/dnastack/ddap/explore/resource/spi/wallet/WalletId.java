package com.dnastack.ddap.explore.resource.spi.wallet;

import com.dnastack.ddap.explore.resource.model.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;

class WalletId extends Id {

    private static final long serialVersionUID = 1311157189773499046L;

    public WalletId() {
    }

    public WalletId(Id id) {
        super(id);
    }

    @JsonIgnore
    public void setName(String resourceId) {
        setAdditionalProperties("w", resourceId);
    }

    @JsonIgnore
    public String getName() {
        return getAdditionalProperties().get("w");
    }

    @JsonIgnore
    public void setInterfaceType(String interfaceType) {
        setAdditionalProperties("i", interfaceType);
    }

    @JsonIgnore
    public String getInterfaceType() {
        return getAdditionalProperties().get("i");
    }


}

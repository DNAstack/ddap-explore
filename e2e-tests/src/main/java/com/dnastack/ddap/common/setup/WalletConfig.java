package com.dnastack.ddap.common.setup;

import static org.hamcrest.MatcherAssert.assertThat;

import lombok.Getter;
import lombok.Setter;
import org.hamcrest.Matchers;

@Getter
@Setter
public class WalletConfig implements ConfigModel {

    private String walletUrl;
    private String adminUserToken;
    private String adminUserEmail;
    private String whitelistUserToken;
    private String whitelistUserEmail;
    private String plainUserToken;
    private String plainUserEmail;
    private String passportIssuer;

    @Override
    public void validateConfig() {
        assertThat(walletUrl, Matchers.notNullValue());
        assertThat(adminUserToken, Matchers.notNullValue());
        assertThat(adminUserEmail, Matchers.notNullValue());
        assertThat(whitelistUserToken, Matchers.notNullValue());
        assertThat(whitelistUserEmail, Matchers.notNullValue());
        assertThat(plainUserToken, Matchers.notNullValue());
        assertThat(plainUserEmail, Matchers.notNullValue());
        assertThat(passportIssuer,Matchers.notNullValue());
    }
}

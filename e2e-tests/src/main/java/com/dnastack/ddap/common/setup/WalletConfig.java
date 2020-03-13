package com.dnastack.ddap.common.setup;

import static org.hamcrest.MatcherAssert.assertThat;

import lombok.Getter;
import lombok.Setter;
import org.hamcrest.Matchers;

@Getter
@Setter
public class WalletConfig implements ConfigModel {


    /**
     * The Wallet URL to use for requests
     */
    private String walletUrl;

    /**
     * The personal access token of a user who has ADMIN privileges within the DDAP environment
     */
    private String adminUserToken;

    /**
     * The email of a user who has ADMIN privileges wihtin the DDAP environment
     */
    private String adminUserEmail;

    /**
     * The personal access token of a user who has Whitelisted privileges within the DDAP environment
     */
    private String whitelistUserToken;

    /**
     * The emai of a user who has Whitelisted privileges within the DDAP environment
     */
    private String whitelistUserEmail;

    /**
     * The personal access token of a user who has No privileges within the DDAP environment
     */
    private String plainUserToken;

    /**
     * The email of a user who has No privileges within the DDAP environment
     */
    private String plainUserEmail;

    /**
     * The Url of the passport issuer. If Used in conjunction with the DAM/IC, this URL refers to the IC
     */
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

package com.dnastack.ddap.explore.resource.spi;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("resources.spi")
public class SpiConfig {

    @NestedConfigurationProperty
    private Map<String, SpiClientConfig> clients;


    @Data
    static class SpiClientConfig {

        /**
         * The Bean name of the specified factory to look up. The name should be defined using the {@link
         * org.springframework.stereotype.Component} annotation on the targeted factory method. The targeted factory
         * must extend the {@link ResourceClientFactory} class
         *
         * @see ResourceClientFactory
         * @see com.dnastack.ddap.explore.resource.spi.dam.ReactiveDamResourceClientFactory
         * @see com.dnastack.ddap.explore.resource.spi.wallet.ReactiveWalletResourceClientFactory
         */
        private String factoryName;
        /**
         * Properties that will be passed into the {@link ResourceClientFactory} to configure the underlying {@link
         * ResourceClient}. The provided <pre>spiProperties</pre> must be convertable to the required Configuration
         * class for the targeted {@link ResourceClientFactory}. Please see each individiaul Implementation for
         * information on how to strcuture the classes
         */
        private Map<String, Object> spiProperties;
    }
}

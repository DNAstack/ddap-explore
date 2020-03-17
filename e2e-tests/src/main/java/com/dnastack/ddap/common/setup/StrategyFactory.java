package com.dnastack.ddap.common.setup;

import static org.junit.Assert.fail;

import com.dnastack.ddap.common.util.EnvUtil;

public class StrategyFactory {

    private static ConfigStrategy configStrategyInstance = null;
    private static LoginStrategy loginStrategy = null;

    public static ConfigStrategy getConfigStrategy() {
        if (configStrategyInstance == null) {
            String configStrategyClassName = EnvUtil
                .optionalEnv("E2E_CONFIG_STRATEGY", NoopConfigStrategy.class.getName());
            try {
                Class<?> configStrategyClass = Class.forName(configStrategyClassName);
                configStrategyInstance = (ConfigStrategy) configStrategyClass.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                fail("Could not find a Config strategy for class: " + configStrategyClassName);
            }
        }
        return configStrategyInstance;
    }


    public static LoginStrategy getLoginStrategy() {
        if (loginStrategy == null) {
            String loginStrategyClassName = EnvUtil
                .optionalEnv("E2E_LOGIN_STRATEGY", PersonaLoginStrategy.class.getName());
            try {
                Class<?> loginStrategyClass = Class.forName(loginStrategyClassName);

                loginStrategy = (LoginStrategy) loginStrategyClass.getConstructor().newInstance();


            } catch (Exception e) {
                throw new AssertionError(
                        "Could not load Login strategy for class: " + loginStrategyClassName,
                        e);
            }
        }
        return loginStrategy;
    }

}

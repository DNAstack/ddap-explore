package com.dnastack.ddap.common.util;

import static org.junit.Assert.fail;

import com.dnastack.ddap.common.setup.ConfigModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class EnvUtil {

    public static String requiredEnv(String name) {
        String val = System.getenv(name);
        if (val == null) {
            fail("Environnment variable `" + name + "` is required");
        }
        return val;
    }

    public static String optionalEnv(String name, String defaultValue) {
        String val = System.getenv(name);
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    public static  <T extends ConfigModel> T requiredEnvConfig(String name, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        String val = requiredEnv(name);
        T config = null;

        try {
            config = mapper.readValue(val, clazz);
            config.validateConfig();
        } catch (IOException e) {
            fail("Environment variable " + name + " could not be converted to type: " + clazz.getName());
        }

        return config;
    }

}

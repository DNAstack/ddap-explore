package com.dnastack.ddap.explore.resource.spi;

import static java.lang.String.format;

import com.dnastack.ddap.explore.resource.spi.SpiConfig.SpiClientConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;


/**
 * An Abstract class for providing dynamic bean creation of resource clients in a way that is context aware, allowing
 * for proper Autowiring of all dependencies. Concrete Subclasses of this Factory class should be annotated with a named
 * {@link Component} annotation. The name specified within the annotation will be used to retrieve the appropriate
 * factory method using {@link #lookupFactory(BeanFactory, String)}.
 *
 * @param <T> Concrete Resource Client Type
 * @param <C> Concrete Configuration Class. This Class must be convertable from a {@code Map<String,Object>} returned by
 * springs Configuration Binding.
 */
public abstract class ResourceClientFactory<T extends ResourceClient, C> {

    protected final ObjectMapper objectMapper;
    private final Logger log;

    protected ResourceClientFactory(ObjectMapper objectMapper, Logger log) {
        this.objectMapper = objectMapper;
        this.log = log;
    }

    /**
     * Validate the converted SpiSpecific configuration prior to generating the bean
     *
     * @param spiKey key for this spi
     * @param spiSpecificConfig the converted config
     */
    protected abstract void validateConfig(String spiKey, C spiSpecificConfig) throws SpiConfigurationException;

    /**
     * Create a new Insteance of the Specified ResourceClientFactory
     */
    protected abstract T create(String spiKey, C spiSpecificConfig) throws SpiConfigurationException;

    /**
     * @return The type of this config, used for deserializing.
     */
    protected abstract TypeReference<C> getConfigType();

    public T create(String spiKey, SpiClientConfig config) throws SpiConfigurationException {
        C convertedConfig;
        try {
            convertedConfig = objectMapper.convertValue(config.getSpiProperties(), getConfigType());
        } catch (RuntimeException e) {
            throw new SpiConfigurationException(format("Unable to parse into type [%s] from value [%s]", getConfigType()
                .getType(), config), e);
        }

        log.info("Creating token enhancer from config [{}]", config);
        validateConfig(spiKey, convertedConfig);
        return create(spiKey, convertedConfig);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ResourceClient, C, F extends ResourceClientFactory<T, C>> F lookupFactory(BeanFactory beanFactory, String factoryBeanName) throws BeansException {
        return (F) beanFactory.getBean(factoryBeanName, ResourceClientFactory.class);
    }

}

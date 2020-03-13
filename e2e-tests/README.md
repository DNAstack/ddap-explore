# DDAP Explore E2E Tests

The DDAP-Explore application has eveolved to support multiple different usecases with different features enabled 
and different backends serving requests. Because of this, the complexity of the tests has steadily evolved to service
the different scenarios that DDAP Explore now services.


## Dependencies

- Java 11


## Building 

The E2E test depend on the `shared` proto resources of this project. Please make sure they are built before trying
to build the E2E tests 

```
../mvnw clean install
```

## Configuration

### Global Configuration

There are a number of shared configuration values that you need to set regardless of the configuration strategy or 
the login strategy that is being used.


| Environment Variable | Required | Default | Description |
| -------------------- | -------- | ------- | ----------- |
| `E2E_BASE_URI`       | `true`   |         | The URI to run the e2e tests against |
| `E2E_TEST_REALM`     | `trye`   |         | The DAM realm to run the ddap requests against. Please not this is required regardless of wether a dam is being used |
| `E2E_BASIC_USERNAME` | `false`  | `null`  | The username used if Basic Auth is enabled |
| `E2E_BASIC_PASSWORD` | `false`  | `null`  | The Password used if Basic Auth is enabled |
| `E2E_TEST_RETRIES`   | `false`  | `3`     | Default number of retries for failed requests | 
| `E2E_TEST_MODE` | `false` | `normal` | The mode to run the tests in. At the moment, only `normal` is supported 
| `E2E_COOKIES_ENCRYPTOR_PASSWORD` | `false` | `abcdefghijk` | The password to use for encyrpting cookies with |
| `E2E_COOKIES_ENCRYPTOR_SALT` | `false` | `598953e322` | The salt used for encrypting a cookie with |
| `E2E_CONFIG_STRATEGY` | `false` | `com.dnastack.ddap.common.setup.NoopConfigStrategy`| The class name of the config strategy to use for one time test setup |
| `E2E_LOGIN_STRATEGY` | `false` | `com.dnastack.ddap.common.setup.PersonaLoginStrategy` | The class name of the login strategy to use for logging users in during tests |
| `E2E_SCREENSHOT_DIR` | `false` | `target` | The directory to save sacreenshots from failed tests to |
| `E2E_SANDBOX` | `false` | `false` | Feature flag for turning on the `RealmE2eTest` |
| `E2E_DATASET_URL` | `false` | `https://storage.googleapis.com/ddap-e2etest-objects/table/dnastack-internal-subjects-with-objects/data` | Publicly accessible Dataset to use for workflows/dataset tests | 
| `E2E_DATASET_SECURED_URL` | `false` | `https://storage.googleapis.com/ddap-e2etest-objects/table/subjects-restricted-access/data` | Privately accessible Dataset to use for workflows/dataset tests | 
| `E2E_WORKFLOW_MAX_WAIT_TIME_IN_MINUTES` | `false` | `2` | The amount of time required to wait for the 



### JSON Configuration Classes

In an effort to simplify the configuration process and reduce the overall number of environment variables that need to be set,
There is now a JSON api that you can use for sepcifying certain configuration paramets as JSON in environment variables. 
Of course this could always be done, however with this approach there is now an encouraged and explicit way for doing this.

To Create a JSON Configuration class simply create a `Data` class and implement the `ConfigModel` interface. This interface
adds a single `.validate()` method, which allows the json config to validate itself.

**Usage**

```bash
E2E_MY_CONFIG='{"someValue":"this thing"}'
```


```java

// Define your config
@Data
public class MyConfig implements ConfigModel {
  
    private String someValue;
    
    private void validate(){
        assertThat(someValue, Matchers.notNullValue());
    }
}

// Get your config from the environment
public void someMethod(){
    //This is alread
    MyConfig config = EnvUtil.requiredEnvConfig("E2E_MY_CONFIG",MyConfig.class);
}

```

  



### Login Strategies

The `LoginStrategy` interface provides a way for the tests to handle cases where `ddap` is deployed in different environments
backed by a different IDP or a different IDP strategy. Creating login strategies is easy, simply implement the `LoginStrategy`
interface with a `NoArgsConstructor`. To specify at run time which strategy should be used, set the env variable: `E2E_LOGIN_STRATEGY`
to the class name of your new strategy

Config Strategies are accessed using the `StrategyFactory.getLoginStrategy()`. This is a static method which will create a singleton instance that is
instantiated exactly once.

#### PersonaLoginStrategy

This strategy is meant to be used in conjunction with the Identity Concentrator's Persona Logins. In general Persona login is
not going to be deployed in production however it has been used to test the integration of services.

**Setup**

```bash
E2E_LOGIN_STRATEGY=com.dnastack.ddap.common.setup.PersonaLoginStrategy
E2E_IC_BASE_URL=<Url pointing to IC>
```

#### DamWalletLoginStrategy

This strategy utilizes both the Dam and Wallet and requires the relevent configuration for both of them. It uses 
wallet/ic as the ultimate IDP but launches many of the initialzation flows (ie for resources) through the dam or the dam's
views api

Setup for this strategy is environment based but predominately relies on [JSON](#json-configuration-classes) configuration
specifically using the [DamConfig](src/main/java/com/dnastack/ddap/common/setup/DamConfig.java) and
[WalletConfig](src/main/java/com/dnastack/ddap/common/setup/WalletConfig.java). Please see each of those classes respectively
for how to setup the config


**Setup**

```bash
E2E_LOGIN_STRATEGY=com.dnastack.ddap.common.setup.DamWalletLoginStrategy
E2E_DAM_CONFIG='{....}'
E2E_WALLET_CONFIG='{....}'

```



### Configuration Strategies

The `ConfigStrategy` interface provides a way for the tests to handle cases where `ddap` is deployed in different environments and
requries specific setup pertaining to that environment. Creating Config strategies is easy, simply implement the `ConfigStrategy`
interface with a `NoArgsConstructor`. To specify at run time which strategy should be used, set the env variable: `E2E_CONFIG_STRATEGY`
to the class name of your new strategy. The `ConfigStrategy` has a single method that is called `doOnetimeSetup`

Config Strategies are accessed using the `StrategyFactory.getConfigStrategy()`. This is a static method which will create a singleton instance that is
instantiated exactly once.


#### NoopConfigStrategy

This is the default strategy and does not run any configuration

**Setup**

```bash
E2E_LOGIN_STRATEGY=com.dnastack.ddap.common.setup.NoopConfigStrategy
```

#### DamConfigStrategy

The DAM config strategy is used in the cases where the test need to configure their environment within the DAM specifically. IE, where
they need to change the `realm` config, seeding the dam with the appropriate resources,claims,trustedsources etc. before running the tests.
It is NOT meant to be used in ALL cases where there is a DAM, ie if the config the tests are using is externalized.

For all configuration values please see: [DamConfig](src/main/java/com/dnastack/ddap/common/setup/DamConfig.java)

**Setup**

```bash
E2E_LOGIN_STRATEGY=com.dnastack.ddap.common.setup.DamConfigStrategy
E2E_DAM_CONFIG='{....}'
```
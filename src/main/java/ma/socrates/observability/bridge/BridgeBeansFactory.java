package ma.socrates.observability.bridge;

import ma.socrates.observability.bridge.api.ConsumerApi;
import ma.socrates.observability.bridge.config.RestClientConfig;
import ma.socrates.observability.bridge.core.client.ConsumerApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BridgeBeansFactory {

    @Bean
    @ConfigurationProperties(prefix = "app.outbound")
    public RestClientConfig createRestClientConfig() {
        return new RestClientConfig();
    }

    @Bean
    public ConsumerApi createConsumerApi(@Autowired RestClientConfig restClientConfig) {
        return new ConsumerApiImpl(restClientConfig.getBaseUrlOf("consumer"));
    }

}

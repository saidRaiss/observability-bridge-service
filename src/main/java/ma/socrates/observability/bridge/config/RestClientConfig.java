package ma.socrates.observability.bridge.config;

import lombok.*;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class RestClientConfig {

    private Map<String, RestClient> restClients;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class RestClient {
        String baseUrl;
    }

    public String getBaseUrlOf(String serviceName) {
        return restClients.get(serviceName).getBaseUrl();
    }
}

package ma.socrates.observability.bridge.core.client;

import ma.socrates.observability.bridge.api.ConsumerApi;
import ma.socrates.observability.bridge.core.model.Message;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public class ConsumerApiImpl implements ConsumerApi {

    private final String baseUrl;
    private final RestClient restClient;

    public ConsumerApiImpl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public Message getMessage(String id) {
        return restClient.get()
                .uri(baseUrl, id)
                .retrieve()
                .body(Message.class);
    }


}

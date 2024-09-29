package ma.socrates.observability.bridge.core.usecase;

import lombok.AllArgsConstructor;
import ma.socrates.observability.bridge.api.ConsumerApi;
import ma.socrates.observability.bridge.core.model.Message;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageRetriever {

    private final ConsumerApi consumerApi;

    public Message handle(String id) {
        return consumerApi.getMessage(id);
    }
}

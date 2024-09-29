package ma.socrates.observability.bridge.api;

import ma.socrates.observability.bridge.core.model.Message;

public interface ConsumerApi {

    Message getMessage(String id);

}

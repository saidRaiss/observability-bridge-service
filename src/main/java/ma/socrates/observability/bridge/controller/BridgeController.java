package ma.socrates.observability.bridge.controller;

import lombok.AllArgsConstructor;
import ma.socrates.observability.bridge.core.model.Message;
import ma.socrates.observability.bridge.core.usecase.MessageRetriever;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BridgeController {

    private final MessageRetriever messageRetriever;

    @GetMapping("/message")
    public Message getMessageById(@RequestParam String id) {
        return messageRetriever.handle(id);
    }
}
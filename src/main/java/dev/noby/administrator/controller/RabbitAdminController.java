package dev.noby.administrator.controller;

import dev.noby.administrator.config.AdminResourceNames;
import dev.noby.administrator.dto.BindingRequest;
import dev.noby.administrator.dto.CreateExchangeRequest;
import dev.noby.administrator.dto.CreateQueueRequest;
import dev.noby.administrator.dto.ExchangeResponse;
import dev.noby.administrator.dto.QueueResponse;
import dev.noby.administrator.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AdminResourceNames.API_ROOT)
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    @ResponseStatus(HttpStatus.CREATED)
    public QueueResponse createQueue(@Valid @RequestBody CreateQueueRequest request) {
        return service.createQueue(request);
    }

    @DeleteMapping("/queues/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQueue(@PathVariable String name) {
        service.deleteQueue(name);
    }

    @PostMapping("/exchanges")
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeResponse createExchange(@Valid @RequestBody CreateExchangeRequest request) {
        return service.createExchange(request);
    }

    @DeleteMapping("/exchanges/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExchange(@PathVariable String name) {
        service.deleteExchange(name);
    }

    @PostMapping("/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    public BindingRequest createBinding(@Valid @RequestBody BindingRequest request) {
        return service.createBinding(request);
    }

    @DeleteMapping("/bindings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBinding(@RequestParam String exchange, @RequestParam String queue,
                              @RequestParam String routingKey) {
        service.deleteBinding(new BindingRequest(exchange, queue, routingKey));
    }
}

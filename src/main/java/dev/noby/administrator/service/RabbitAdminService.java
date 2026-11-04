package dev.noby.administrator.service;

import dev.noby.administrator.config.AdminResourceNames;
import dev.noby.administrator.dto.BindingRequest;
import dev.noby.administrator.dto.CreateExchangeRequest;
import dev.noby.administrator.dto.CreateQueueRequest;
import dev.noby.administrator.dto.ExchangeResponse;
import dev.noby.administrator.dto.QueueResponse;
import dev.noby.administrator.exception.InvalidAdminRequestException;
import dev.noby.administrator.exception.ProtectedResourceException;
import dev.noby.administrator.exception.ResourceConflictException;
import dev.noby.administrator.exception.ResourceMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin admin;

    public RabbitAdminService(AmqpAdmin admin) {
        this.admin = admin;
    }

    public QueueResponse createQueue(CreateQueueRequest request) {
        validateResourceName(request.name());
        if (admin.getQueueProperties(request.name()) != null) {
            throw new ResourceConflictException("La cola ya existe: " + request.name());
        }
        Queue queue = QueueBuilder.durable(request.name())
                .withArgument("x-queue-type", "classic")
                .build();
        admin.declareQueue(queue);
        log.info("Created demo queue {}", request.name());
        return new QueueResponse(request.name(), "classic", true);
    }

    public void deleteQueue(String name) {
        validateResourceName(name);
        if (!admin.deleteQueue(name)) {
            throw new ResourceMissingException("La cola no existe: " + name);
        }
        log.info("Deleted demo queue {}", name);
    }

    public ExchangeResponse createExchange(CreateExchangeRequest request) {
        validateResourceName(request.name());
        if (request.type() == null) {
            throw new InvalidAdminRequestException("type debe ser direct o topic");
        }
        switch (request.type()) {
            case "direct" -> admin.declareExchange(new DirectExchange(request.name(), true, false));
            case "topic" -> admin.declareExchange(new TopicExchange(request.name(), true, false));
            default -> throw new InvalidAdminRequestException("type debe ser direct o topic");
        }
        log.info("Declared demo {} exchange {}", request.type(), request.name());
        return new ExchangeResponse(request.name(), request.type(), true);
    }

    public void deleteExchange(String name) {
        validateResourceName(name);
        if (!admin.deleteExchange(name)) {
            throw new ResourceMissingException("El exchange no existe: " + name);
        }
        log.info("Deleted demo exchange {}", name);
    }

    public BindingRequest createBinding(BindingRequest request) {
        validateBinding(request);
        requireQueue(request.queue());
        admin.declareBinding(binding(request));
        log.info("Bound demo queue {} to exchange {} with key {}", request.queue(), request.exchange(),
                request.routingKey());
        return request;
    }

    public void deleteBinding(BindingRequest request) {
        validateBinding(request);
        requireQueue(request.queue());
        admin.removeBinding(binding(request));
        log.info("Removed demo binding {} -> {} with key {}", request.exchange(), request.queue(),
                request.routingKey());
    }

    private Binding binding(BindingRequest request) {
        return new Binding(request.queue(), Binding.DestinationType.QUEUE, request.exchange(),
                request.routingKey(), null);
    }

    private void requireQueue(String name) {
        if (admin.getQueueProperties(name) == null) {
            throw new ResourceMissingException("La cola no existe: " + name);
        }
    }

    private void validateBinding(BindingRequest request) {
        if (request == null) {
            throw new InvalidAdminRequestException("Faltan datos del binding");
        }
        validateResourceName(request.exchange());
        validateResourceName(request.queue());
        if (request.routingKey() == null || request.routingKey().length() > 128
                || !AdminResourceNames.ROUTING_KEY.matcher(request.routingKey()).matches()) {
            throw new InvalidAdminRequestException("routingKey debe comenzar con demo. y contener segmentos válidos");
        }
    }

    private void validateResourceName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidAdminRequestException("El nombre no puede estar vacío");
        }
        if (!name.startsWith(AdminResourceNames.DEMO_PREFIX)) {
            throw new ProtectedResourceException("Solo se pueden administrar recursos demo.*");
        }
        if (name.length() > 80 || !AdminResourceNames.RESOURCE.matcher(name).matches()) {
            throw new InvalidAdminRequestException("El nombre demo.* contiene caracteres o segmentos inválidos");
        }
    }
}

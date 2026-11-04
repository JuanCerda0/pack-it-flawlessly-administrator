package dev.noby.administrator;

import dev.noby.administrator.dto.BindingRequest;
import dev.noby.administrator.dto.CreateExchangeRequest;
import dev.noby.administrator.dto.CreateQueueRequest;
import dev.noby.administrator.exception.ProtectedResourceException;
import dev.noby.administrator.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@EnabledIfSystemProperty(named = "admin.rabbit.integration", matches = "true")
class RabbitAdminBrokerTests {

    @Autowired RabbitAdminService service;
    @Autowired AmqpAdmin admin;
    @Autowired RabbitTemplate rabbitTemplate;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void declaresRoutesAndDeletesOnlyDemoResources() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String exchange = "demo." + suffix + ".exchange";
        String queue = "demo." + suffix + ".queue";
        String key = "demo." + suffix + ".created";
        try {
            assertEquals("classic", service.createQueue(new CreateQueueRequest(queue)).type());
            assertEquals("topic", service.createExchange(new CreateExchangeRequest(exchange, "topic")).type());
            service.createBinding(new BindingRequest(exchange, queue, key));
            rabbitTemplate.convertAndSend(exchange, key, "hello");
            Object message = rabbitTemplate.receiveAndConvert(queue, 3000);
            assertEquals("hello", message);
            assertThrows(ProtectedResourceException.class, () -> service.deleteQueue("packit.events"));
            assertNotNull(admin.getQueueProperties(queue));
            service.deleteBinding(new BindingRequest(exchange, queue, key));
            service.deleteQueue(queue);
            service.deleteExchange(exchange);
            assertNull(admin.getQueueProperties(queue));
        } finally {
            admin.deleteQueue(queue);
            admin.deleteExchange(exchange);
        }
    }
}

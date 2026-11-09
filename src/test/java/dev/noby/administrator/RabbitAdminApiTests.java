package dev.noby.administrator;

import dev.noby.administrator.config.CognitoGroupAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RabbitAdminApiTests {

    @Autowired MockMvc mvc;
    @MockitoBean AmqpAdmin amqpAdmin;
    @MockitoBean JwtDecoder jwtDecoder;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token(String group) {
        return jwt().jwt(builder -> builder.claim("cognito:groups", List.of(group)))
                .authorities(new CognitoGroupAuthoritiesConverter());
    }

    @Test
    void onlyAdminCanChangeBrokerResources() throws Exception {
        String body = "{\"name\":\"demo.sample\"}";
        mvc.perform(post("/api/v1/rabbit/queues").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/rabbit/queues").with(token("SUPERVISOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        verifyNoInteractions(amqpAdmin);
    }

    @Test
    void rejectsProtectedAndMalformedNamesBeforeRabbit() throws Exception {
        mvc.perform(post("/api/v1/rabbit/queues").with(token("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"packit.events\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/rabbit/queues").with(token("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"demo..bad\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/rabbit/exchanges").with(token("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"demo.x\",\"type\":\"fanout\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(amqpAdmin);
    }
}

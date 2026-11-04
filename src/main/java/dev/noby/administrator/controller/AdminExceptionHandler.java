package dev.noby.administrator.controller;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ShutdownSignalException;
import dev.noby.administrator.dto.ApiError;
import dev.noby.administrator.exception.InvalidAdminRequestException;
import dev.noby.administrator.exception.ProtectedResourceException;
import dev.noby.administrator.exception.ResourceConflictException;
import dev.noby.administrator.exception.ResourceMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AdminExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminExceptionHandler.class);

    @ExceptionHandler({InvalidAdminRequestException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> invalidRequest(Exception error) {
        return ResponseEntity.badRequest().body(new ApiError(error.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException error) {
        String message = error.getBindingResult().getFieldErrors().stream()
                .findFirst().map(field -> field.getField() + ": " + field.getDefaultMessage())
                .orElse("Solicitud inválida");
        return ResponseEntity.badRequest().body(new ApiError(message));
    }

    @ExceptionHandler({ProtectedResourceException.class, ResourceConflictException.class})
    ResponseEntity<ApiError> conflict(RuntimeException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(error.getMessage()));
    }

    @ExceptionHandler(ResourceMissingException.class)
    ResponseEntity<ApiError> missing(ResourceMissingException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(error.getMessage()));
    }

    @ExceptionHandler(AmqpException.class)
    ResponseEntity<ApiError> brokerFailure(AmqpException error) {
        log.warn("RabbitMQ administration failed", error);
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof ShutdownSignalException signal
                    && signal.getReason() instanceof AMQP.Channel.Close close) {
                if (close.getReplyCode() == 404) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiError("El recurso de RabbitMQ no existe"));
                }
                if (close.getReplyCode() == 406) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new ApiError("El recurso existe con una configuración incompatible"));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("RabbitMQ no está disponible para la operación"));
    }
}

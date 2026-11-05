# PackIt — Administrador RabbitMQ

API Spring Boot 4.1.2 (Java 21) para administrar una topología de demostración en RabbitMQ. No usa base de datos. La topología de Paquetes, Historial y Documentos permanece bajo la configuración de esos servicios.

## Ejecutar en local

Con RabbitMQ activo en `localhost:5672`, ejecute `./mvnw spring-boot:run`. La API escucha en `http://localhost:8084`. Las variables `ADMIN_RABBITMQ_HOST`, `ADMIN_RABBITMQ_PORT`, `ADMIN_RABBITMQ_USERNAME` y `ADMIN_RABBITMQ_PASSWORD` reemplazan la conexión predeterminada `guest/guest`. `PORT`, `COGNITO_ISSUER_URI` y `ADMIN_CORS_ALLOWED_ORIGINS` configuran HTTP, Cognito y CORS.

Todas las operaciones requieren un **access token de Cognito** con `cognito:groups` que incluya `ADMIN`. El grupo `SUPERVISOR` no puede modificar recursos. Solo se aceptan nombres `demo.*`, para impedir cambios en las colas y exchanges de trabajo. Las colas creadas son `classic` durables; los exchanges son `direct` o `topic` durables.

## API

Use `Authorization: Bearer <access-token>` y `Content-Type: application/json`:

| Operación | Petición |
| --- | --- |
| Crear cola | `POST /api/v1/rabbit/queues` con `{"name":"demo.pedidos"}` |
| Borrar cola | `DELETE /api/v1/rabbit/queues/demo.pedidos` |
| Crear exchange | `POST /api/v1/rabbit/exchanges` con `{"name":"demo.eventos","type":"topic"}` |
| Borrar exchange | `DELETE /api/v1/rabbit/exchanges/demo.eventos` |
| Crear binding | `POST /api/v1/rabbit/bindings` con `{"exchange":"demo.eventos","queue":"demo.pedidos","routingKey":"demo.pedido.creado"}` |
| Borrar binding | `DELETE /api/v1/rabbit/bindings?exchange=demo.eventos&queue=demo.pedidos&routingKey=demo.pedido.creado` |

Las creaciones devuelven `201`; las eliminaciones, `204`. Entradas inválidas devuelven `400`, recursos inexistentes `404`, nombres protegidos o configuraciones incompatibles `409`, y una falla de conexión al broker `503`. Declarar otra vez el mismo exchange o binding con la misma configuración es una operación idempotente; borrar un binding ya ausente también devuelve `204` si la cola existe.

## Pruebas

`./mvnw test` ejecuta las pruebas de API, validación y autorización sin requerir RabbitMQ. Con un broker local activo, `./mvnw -Dadmin.rabbit.integration=true -Dtest=RabbitAdminBrokerTests test` verifica creación, enrutamiento y eliminación de recursos `demo.*`. La prueba usa nombres aleatorios y limpia sus recursos.

## Docker local

El [Compose del repositorio Paquetes](../pack-it-flawlessly-backend/compose.yaml) construye este servicio junto con los demás. Comparte el clúster RabbitMQ y no recibe una base PostgreSQL. Consulta el README de Paquetes para preparar `.env.compose` y levantar el stack.

package ru.muravin.bankapplication.gateway.configurations;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class LoggingFilterConfiguration implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Оборачиваем оригинальный запрос
        ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {

            private boolean bodyLogged = false;

            @Override
            public Flux<DataBuffer> getBody() {
                return super.getBody().map(dataBuffer -> {
                    if (!bodyLogged) {
                        // Читаем содержимое DataBuffer
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.asByteBuffer().get(content); // безопасное чтение
                        String body = new String(content, StandardCharsets.UTF_8);

                        // Логируем тело запроса
                        System.out.println("Request Body: " + body);
                        bodyLogged = true;

                        // Возвращаем буфер обратно в цепочку обработки
                        return dataBuffer;
                    }
                    return dataBuffer;
                });
            }
        };

        // Заменяем исходный запрос на наш декорированный
        return chain.filter(exchange.mutate().request(decoratedRequest).build());
    }
}
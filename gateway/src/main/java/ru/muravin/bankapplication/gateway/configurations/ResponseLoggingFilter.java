package ru.muravin.bankapplication.gateway.configurations;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ResponseLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Оборачиваем оригинальный response
        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

            private final StringBuilder responseBody = new StringBuilder();

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<DataBuffer> fluxBody = (Flux<DataBuffer>) body;

                    return super.writeWith(fluxBody.doOnNext(dataBuffer -> {
                        // Читаем содержимое DataBuffer
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        // Сохраняем копию данных перед отправкой клиенту
                        String data = new String(content, StandardCharsets.UTF_8);
                        responseBody.append(data);

                        // После чтения, возвращаем буфер обратно, чтобы он мог быть использован дальше
                        DataBufferUtils.release(dataBuffer);
                    }).doOnComplete(() -> {
                        // Логируем тело ответа после завершения
                        if (responseBody.length() > 0) {
                            System.out.println("Response Body: " + responseBody.toString());
                        }
                    }));
                }
                return super.writeWith(body);
            }
        };

        // Заменяем response на наш декорированный
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
}

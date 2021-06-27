package com.untanglechat.chatapp.dto.response;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import lombok.Data;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;


@Data
public class FluxResponse {
    final CompletableFuture<FluxResponse> cf = new CompletableFuture<>();
    GetObjectResponse sdkResponse;
    Flux<ByteBuffer> flux;
}
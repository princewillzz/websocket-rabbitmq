package com.untanglechat.chatapp.dto.response;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import reactor.core.publisher.Flux;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;



public class FluxResponseProvider implements AsyncResponseTransformer<GetObjectResponse,FluxResponse> {    
    private FluxResponse response;
    @Override
    public CompletableFuture<FluxResponse> prepare() {
        response = new FluxResponse();
        return response.cf;
    }

    @Override
    public void onResponse(GetObjectResponse sdkResponse) {            
        this.response.sdkResponse = sdkResponse;
    }

    @Override
    public void onStream(SdkPublisher<ByteBuffer> publisher) {
        response.flux = Flux.from(publisher);
        response.cf.complete(response);            
    }

    @Override
    public void exceptionOccurred(Throwable error) {
        response.cf.completeExceptionally(error);
    }
}
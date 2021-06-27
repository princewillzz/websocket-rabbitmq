package com.untanglechat.chatapp.services.aws;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.untanglechat.chatapp.dto.response.FluxResponse;
import com.untanglechat.chatapp.dto.response.FluxResponseProvider;
import com.untanglechat.chatapp.exceptions.UploadFailedException;
import com.untanglechat.chatapp.properties.S3ClientConfigurarionProperties;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonAWSS3Service {


    private final S3AsyncClient s3client;
    private final S3ClientConfigurarionProperties s3config;



    public Mono<String> saveFile(HttpHeaders headers, String bucket, FilePart part) {
        String filekey = UUID.randomUUID().toString();
        Map<String, String> metadata = new HashMap<String, String>();
        String filename = part.filename();
        if ( filename == null ) {
            filename = filekey;
        }       
        metadata.put("filename", filename);    
        MediaType mt = part.headers().getContentType();
        if ( mt == null ) {
            mt = MediaType.APPLICATION_OCTET_STREAM;
        }
        UploadState uploadState = new UploadState(bucket,filekey);     
        CompletableFuture<CreateMultipartUploadResponse> uploadRequest = s3client
          .createMultipartUpload(CreateMultipartUploadRequest.builder()
            .contentType(mt.toString())
            .key(filekey)
            .metadata(metadata)
            .bucket(bucket)
            .build());
    
            return Mono
            .fromFuture(uploadRequest)
            .flatMapMany((response) -> {
                checkResult(response);              
                uploadState.uploadId = response.uploadId();
                return part.content();
            })
            .bufferUntil((buffer) -> {
                uploadState.buffered += buffer.readableByteCount();
                if ( uploadState.buffered >= s3config.getMultipartMinPartSize() ) {
                    uploadState.buffered = 0;
                    return true;
                } else {
                    return false;
                }
            })
            .map((buffers) -> concatBuffers(buffers))
            .flatMap((buffer) -> uploadPart(uploadState,buffer))
            .reduce(uploadState,(state,completedPart) -> {
                state.completedParts.put(completedPart.partNumber(), completedPart);              
                return state;
            })
            .flatMap((state) -> completeUpload(state))
            .map((response) -> {
                checkResult(response);
                return  uploadState.filekey;
            });
    }

    private static ByteBuffer concatBuffers(List<DataBuffer> buffers) {
        log.info("[I198] creating BytBuffer from {} chunks", buffers.size());
        
        int partSize = 0;
        for( DataBuffer b : buffers) {
            partSize += b.readableByteCount();                  
        }
        
        ByteBuffer partData = ByteBuffer.allocate(partSize);
        buffers.forEach((buffer) -> {
           partData.put(buffer.asByteBuffer());
        });
        
        // Reset read pointer to first byte
        partData.rewind();
        
        log.info("[I208] partData: size={}", partData.capacity());
        return partData;
        
    }


     

       /**
     * Upload a single file part to the requested bucket
     * @param uploadState
     * @param buffer
     * @return
     */
    private Mono<CompletedPart> uploadPart(UploadState uploadState, ByteBuffer buffer) {
        final int partNumber = ++uploadState.partCounter;
        log.info("[I218] uploadPart: partNumber={}, contentLength={}",partNumber, buffer.capacity());

        CompletableFuture<UploadPartResponse> request = s3client.uploadPart(UploadPartRequest.builder()
            .bucket(uploadState.bucket)
            .key(uploadState.filekey)
            .partNumber(partNumber)
            .uploadId(uploadState.uploadId)
            .contentLength((long) buffer.capacity())
            .build(), 
            AsyncRequestBody.fromPublisher(Mono.just(buffer)));
        
        return Mono
          .fromFuture(request)
          .map((uploadPartResult) -> {              
              checkResult(uploadPartResult);
              log.info("[I230] uploadPart complete: part={}, etag={}",partNumber,uploadPartResult.eTag());
              return CompletedPart.builder()
                .eTag(uploadPartResult.eTag())
                .partNumber(partNumber)
                .build();
          });
    }

    private Mono<CompleteMultipartUploadResponse> completeUpload(UploadState state) {        
        log.info("[I202] completeUpload: bucket={}, filekey={}, completedParts.size={}", state.bucket, state.filekey, state.completedParts.size());        

        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
            .parts(state.completedParts.values())
            .build();

        return Mono.fromFuture(s3client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
            .bucket(state.bucket)
            .uploadId(state.uploadId)
            .multipartUpload(multipartUpload)
            .key(state.filekey)
            .build()));
    }


    /**
     * Holds upload state during a multipart upload
     */
    static class UploadState {
        final String bucket;
        final String filekey;

        String uploadId;
        int partCounter;
        Map<Integer, CompletedPart> completedParts = new HashMap<>();
        int buffered = 0;

        UploadState(String bucket, String filekey) {
            this.bucket = bucket;
            this.filekey = filekey;
        }
    }
    

    public Mono<FluxResponse>  downloadFile(final String filekey) {
        GetObjectRequest request = GetObjectRequest.builder()
        .bucket(s3config.getBucket())
        .key(filekey)
        .build();



        return Mono.fromFuture(s3client.getObject(request, new FluxResponseProvider()))
                .map(response -> {
                    checkResult(response.getSdkResponse());
                    return response;
                });
    }

     /**
     * check result from an API call.
     * @param result Result from an API call
     */
    public static void checkResult(SdkResponse result) {
        if (result.sdkHttpResponse() == null || !result.sdkHttpResponse().isSuccessful()) {
            throw new UploadFailedException(result);
        }        
    }

}

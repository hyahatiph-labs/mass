package com.hiahatf.mass.services.rpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ssl.SSLException;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service("LightningRpc")
public class Lightning {
    
    private String lndHost;
    private String macaroonPath;

    /**
     * Lightning RPC constructor
     * @param host
     * @param macaroonPath
     */
    public Lightning(@Value("${host.lightning}") String host,
    @Value("${macaroon-path}") String macaroonPath) {
        this.lndHost = host;
        this.macaroonPath = macaroonPath;
    }

    /**
     * Testing LND connectivity
     * @throws SSLException
     */
    public Mono<String> getInfo() throws SSLException, IOException {
        Path path = Paths.get(macaroonPath);
        byte[] byteArray = Files.readAllBytes(path);
        String macaroonHex = Hex.encodeHexString(byteArray);
        // work around for the self-signed TLS cert
        SslContext sslContext = SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));

        // lightning rpc web client
        WebClient client = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(lndHost).build();
        return client.get()
            .uri(uriBuilder -> uriBuilder
            .path("/v1/getinfo").build())
            .header("Grpc-Metadata-macaroon", macaroonHex)
            .retrieve()
            .bodyToMono(String.class);
    }

}

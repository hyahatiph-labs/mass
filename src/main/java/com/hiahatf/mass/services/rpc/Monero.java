package com.hiahatf.mass.services.rpc;

import org.slf4j.LoggerFactory;

import com.hiahatf.mass.models.MoneroValidateAddressParameters;
import com.hiahatf.mass.models.MoneroValidateAddressRequest;
import com.hiahatf.mass.models.MoneroValidateAddressResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service("MoneroRpc")
public class Monero {
    
    // logger
    private Logger logger = LoggerFactory.getLogger(Monero.class);
    private String moneroHost;

    /**
     * Monero RPC constructor
     * @param host
     */
    public Monero(@Value("${host.monero}") String host) {
        this.moneroHost = host;
    }

    /**
     * Make the Monero validate_address RPC call
     * Use blocking logic on web client to perform validation.
     * @param address
     * @return
     */
    public Mono<MoneroValidateAddressResponse> validateAddress(String address) {
        // build request
        MoneroValidateAddressParameters params = MoneroValidateAddressParameters
            .builder().address(address).build();
        MoneroValidateAddressRequest request = MoneroValidateAddressRequest
            .builder().params(params).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        Mono<MoneroValidateAddressResponse> res = client.post()
            .uri(uriBuilder -> uriBuilder
            .path("json_rpc").build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(MoneroValidateAddressResponse.class);
            res.subscribe(r -> logger.info("XMR validate address response: {}", r));
        return res;
    }

}

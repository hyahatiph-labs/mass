package com.hiahatf.mass.services.rpc;

import com.hiahatf.mass.models.MoneroValidateAddressParameters;
import com.hiahatf.mass.models.MoneroValidateAddressRequest;
import com.hiahatf.mass.models.MoneroValidateAddressResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service("MoneroRpc")
public class Monero {
    
    private String moneroHost;

    /**
     * Monero RPC constructor
     * @param host
     */
    public Monero(@Value("${host.monero}") String host) {
        this.moneroHost = host;
    }

    /**
     * Make the Monero validate_address RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
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
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path("json_rpc").build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(MoneroValidateAddressResponse.class);
    }

}

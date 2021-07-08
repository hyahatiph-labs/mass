package com.hiahatf.mass.services;

import com.hiahatf.mass.models.SwapRequest;
import com.hiahatf.mass.models.SwapResponse;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service("SwapService")
public class SwapService {
    
    // TODO: fetch quote, if not fulfilled

    // TODO: verify inflight payment

    // TODO: send monero

    // TODO: if xmr rpc success
    // settle hold invoice

    // TODO: update quote to fulfilled

    // TODO: response:
    // xmr tx id and quoteId

    public Mono<SwapResponse> processMoneroSwap(SwapRequest request) {
        return Mono.just(SwapResponse.builder().build());
    }
    
}

package com.hiahatf.mass.services.rate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Update price data on a recurring basis
 */
@Service("RateService")
public class RateService {

    // update price every 10 min.
    private Logger logger = LoggerFactory.getLogger(RateService.class);
    private static final int FREQUENCY = 600000;
    private static final int INITIAL_DELAY = 10000;
    private Mono<String> moneroRate;
    
    private String xmrPriceUrl;

    public RateService(@Value("${host.price}") String url) {
        this.xmrPriceUrl = url;
    }

    /**
     * Accessor for the Monero rate
     * @return monero rate
     */
    public Mono<String> getMoneroRate() {
        return this.moneroRate;
    }

    /**
     * This method updates the Monero price feed
     * with Spring Scheduling. Use accessor to get the 
     * most recent data.
     */
    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = FREQUENCY)
    public void updateMoneroRate() {
    logger.info("Updating XMR <-> BTC rate");
        // Monero rate web client
        WebClient client = WebClient.builder().baseUrl(xmrPriceUrl).build();
        Mono<String> xmrRate = client.get()
        .uri(uriBuilder -> uriBuilder
            .path("/data/price")
            .queryParam("fsym", "XMR")
            .queryParam("tsyms", "BTC")
            .build())
        .retrieve()
        .bodyToMono(String.class);
        this.moneroRate = xmrRate.retry(1);
    }

}

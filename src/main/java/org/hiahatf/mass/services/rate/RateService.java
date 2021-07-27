package org.hiahatf.mass.services.rate;

import org.hiahatf.mass.models.Constants;
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
@Service
public class RateService {

    // update price every 10 min.
    private Logger logger = LoggerFactory.getLogger(RateService.class);
    private static final int FREQUENCY = 600000;
    private static final int INITIAL_DELAY = 10000;
    private String moneroRate;
    
    private String xmrPriceUrl;

    public RateService(@Value(Constants.RATE_HOST) String url) {
        this.xmrPriceUrl = url;
    }

    /**
     * Accessor for the Monero rate
     * @return monero rate
     */
    public String getMoneroRate() {
        return this.moneroRate;
    }

    /**
     * This method updates the Monero price feed
     * with Spring Scheduling. Use accessor to get the 
     * most recent data.
     */
    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = FREQUENCY)
    public void updateMoneroRate() {
    logger.info(Constants.UPDATE_RATE_MSG);
        // Monero rate web client
        WebClient client = WebClient.builder().baseUrl(xmrPriceUrl).build();
        Mono<String> xmrRate = client.get()
        .uri(uriBuilder -> uriBuilder
            .path(Constants.RATE_PATH)
            .queryParam(Constants.RATE_FROM, Constants.XMR)
            .queryParam(Constants.RATE_TO, Constants.BTC)
            .build())
        .retrieve()
        .bodyToMono(String.class);
        // normally wouldn't use block, but is needed here to cache price data
        this.moneroRate = xmrRate.retry(1).block();
    }

}

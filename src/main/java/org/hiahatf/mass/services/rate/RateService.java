package org.hiahatf.mass.services.rate;

import org.hiahatf.mass.models.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider.Proxy;

/**
 * Update price data on a recurring basis
 */
@Service
public class RateService {

    // update price every 10 min.
    private Logger logger = LoggerFactory.getLogger(RateService.class);
    private static final int FREQUENCY = 600000;
    private static final int INITIAL_DELAY = 10000;
    private boolean bypassProxy;
    private String moneroRate;
    private String xmrPriceUrl;
    private int port;

    public RateService(@Value(Constants.RATE_HOST) String url,
    @Value(Constants.RATE_PORT) int port,
    @Value(Constants.PROXY_BYPASS) boolean bypassProxy) {
        this.bypassProxy = bypassProxy;
        this.xmrPriceUrl = url;
        this.port = port;
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
        HttpClient httpClient = HttpClient.create()
            .proxy(proxy -> proxy.type(Proxy.HTTP).host(Constants.LOCALHOST).port(port));
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        // Monero rate web client
        WebClient client = WebClient.builder().baseUrl(xmrPriceUrl)
        .clientConnector(connector).build();
        // TODO: find out how to test the proxy code
        if (bypassProxy) {
            client = WebClient.builder().baseUrl(xmrPriceUrl).build();
        }
        Mono<String> xmrRate = client.get().retrieve().bodyToMono(String.class);
        // normally wouldn't use block, but is needed here to cache price data
        this.moneroRate = xmrRate.retry(1).block();
    }

}

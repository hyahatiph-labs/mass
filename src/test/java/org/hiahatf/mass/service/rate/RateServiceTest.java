package org.hiahatf.mass.service.rate;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;

import org.hiahatf.mass.services.rate.RateService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.handler.codec.http.HttpHeaderValues;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Tests for Rate Service
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class RateServiceTest {
    
    public static MockWebServer mockBackEnd;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RateService rateService;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        rateService = new RateService(baseUrl, 4444, true);
    }

    @Test
    @DisplayName("Rate Service Test")
    public void getRateTest() throws JsonProcessingException {
        String expectedRate = "{\"BTC\":\"0.00777\"}";
        HashMap<String,String> res = new HashMap<>();
        res.put("BTC", "0.00777");
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(res))
            .addHeader(HttpHeaders.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON.toString()));
        // bypass proxy and test code without proxy
        rateService.updateMoneroRate();
        String testRate = rateService.getMoneroRate();
        assertEquals(expectedRate, testRate);
    }
    
}

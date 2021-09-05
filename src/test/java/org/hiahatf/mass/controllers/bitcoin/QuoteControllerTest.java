package org.hiahatf.mass.controllers.bitcoin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.hiahatf.mass.services.bitcoin.QuoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class QuoteControllerTest {
    
    @Mock
    QuoteService service;

    @InjectMocks
    QuoteController controller = new QuoteController(service);

    @Test
    @DisplayName("Quote Controller Test")
    public void fetchBitcoinQuoteTest() {
        List<String> infos = Lists.newArrayList();
        Request request = Request.builder().amount(0.1)
            .proofAddress("proofAddress").proofSignature("proofSignature")
            .refundAddress("refundAddress").swapMultisigInfos(infos).build();
        Quote mockQuote = Quote.builder().quoteId("quoteId").build();
        when(service.processBitcoinQuote(request)).thenReturn(Mono.just(mockQuote));
        Mono<Quote> testQuote = controller.fetchBitcoinQuote(request);
        assertEquals(mockQuote.getQuoteId(), testQuote.block().getQuoteId());
    }
}

package org.hiahatf.mass.controllers.monero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.hiahatf.mass.models.monero.Quote;
import org.hiahatf.mass.models.monero.Request;
import org.hiahatf.mass.services.monero.QuoteService;
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
    QuoteService quoteService;

    @InjectMocks
    QuoteController controller = new QuoteController(quoteService);

    @Test
    @DisplayName("Quote Controller Test")
    public void fetchMoneroQuoteTest() {
        String address = "54xxx";
        Request request = Request.builder().address(address).build();
        Quote quote = Quote.builder().address(address).build();
        when(quoteService.processMoneroQuote(request)).thenReturn(Mono.just(quote));
        Mono<Quote> testQuote = controller.fetchMoneroQuote(request);
        assertEquals(quote.getAddress(), testQuote.block().getAddress());
    }
    
}

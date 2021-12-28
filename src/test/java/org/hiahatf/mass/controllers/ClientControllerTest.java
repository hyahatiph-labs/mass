package org.hiahatf.mass.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.hiahatf.mass.models.monero.Quote;
import org.hiahatf.mass.models.monero.Request;
import org.hiahatf.mass.services.monero.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ClientControllerTest {
    
    @Mock
    ClientService clientService;

    @InjectMocks
    ClientController controller = new ClientController(clientService);

    @Test
    @DisplayName("Client Controller Quote Test")
    public void clientQuoteTest() {
        Quote quote = Quote.builder().destAddress("destAddress").build();
        Request request = Request.builder().address("address").build();
        when(clientService.relayQuote(request)).thenReturn(Mono.just(quote));
        Mono<Quote> testQuote = controller.generateMoneroQuote(request);
        assertEquals(quote.getDestAddress(), testQuote.block().getDestAddress());
    }

}

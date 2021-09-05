// package org.hiahatf.mass.service.bitcoin;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyDouble;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.when;

// import java.io.IOException;

// import javax.net.ssl.SSLException;

// import org.hiahatf.mass.models.bitcoin.Quote;
// import org.hiahatf.mass.models.bitcoin.Request;
// import org.hiahatf.mass.models.lightning.PaymentRequest;
// import org.hiahatf.mass.repo.BitcoinQuoteRepository;
// import org.hiahatf.mass.services.bitcoin.QuoteService;
// import org.hiahatf.mass.services.rate.RateService;
// import org.hiahatf.mass.services.rpc.Lightning;
// import org.hiahatf.mass.services.rpc.Monero;
// import org.hiahatf.mass.util.MassUtil;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.platform.runner.JUnitPlatform;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import reactor.core.publisher.Mono;
// import reactor.test.StepVerifier;

// /**
//  * Tests for Bitcoin Quote Service
//  */
// @ExtendWith(MockitoExtension.class)
// @RunWith(JUnitPlatform.class)
// public class QuoteServiceTest {
    
//     @Mock
//     private RateService rateService;
//     @Mock
//     private Monero monero;
//     @Mock
//     private Lightning lightning;
//     @Mock
//     private MassUtil massUtil;
//     @Mock
//     private BitcoinQuoteRepository quoteRepository;
//     private final Long minPay = 10000L;
//     private final Long maxPay = 1000000L;
//     @InjectMocks
//     private QuoteService quoteService = new QuoteService(quoteRepository,
//         "54send", minPay, maxPay, massUtil, rateService, "test", monero);

//     @Test
//     @DisplayName("Bitcoin Quote Service Test")
//     public void processBitcoinQuoteTest() throws SSLException, IOException {
//         Request request = Request.builder()
//             .refundAddress("54refund")
//             .build();
//         PaymentRequest pr = PaymentRequest.builder()
//             .num_satoshis("100000")
//             .payment_hash("hash")
//             .expiry("600")
//             .build();

//         // mocks
//         when(rateService.getMoneroRate()).thenReturn("{BTC: 0.00777}");
//         when(massUtil.parseMoneroRate(anyString())).thenReturn(0.008);
//         when(massUtil.validateLiquidity(anyDouble(), any()))
//             .thenReturn(Mono.just(true));

//         Mono<Quote> testQuote = quoteService.processBitcoinQuote(request);

//         StepVerifier.create(testQuote)
//         .expectNextMatches(q -> q.getQuoteId()
//           .equals(pr.getPayment_hash()))
//         .verifyComplete();
//     }
    
// }

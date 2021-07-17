package org.hiahatf.mass.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.hiahatf.mass.models.lightning.Info;
import org.hiahatf.mass.services.rpc.Lightning;
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
public class HealthControllerTest {
    
    @Mock
    Lightning lightning;

    @InjectMocks
    HealthController controller = new HealthController(lightning);

    @Test
    @DisplayName("Health Controller Test")
    public void fetchMoneroQuoteTest() throws IOException {
        Info info = Info.builder().version("v0.0.0").build();
        when(lightning.getInfo()).thenReturn(Mono.just(info));
        Mono<Info> testInfo = controller.ping();
        assertEquals(info.getVersion(), testInfo.block().getVersion());
    }
    
}

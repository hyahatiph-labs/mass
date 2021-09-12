package org.hiahatf.mass.controllers;

import java.io.IOException;

import org.hiahatf.mass.models.Integrity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class IntegrityControllerTest {
    
    private IntegrityController controller = new IntegrityController();

    @Test
    @DisplayName("Integrity Test")
    public void integrityTest() throws IOException {
        String expectedHash = "93c53eedd4fd32d832a4d4086f42ba8397d2d1690242183c128785a6e8969c7c";

        Mono<Integrity> testIntegrity = controller.validateIntegrity();

        StepVerifier.create(testIntegrity)
        .expectNextMatches(r -> r.getIntegrity()
          .equals(expectedHash))
        .verifyComplete();
    }
    
}

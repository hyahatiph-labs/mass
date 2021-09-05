package org.hiahatf.mass.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class BaseControllerTest {
    
    @Test
    @DisplayName("Mass Exception Test")
    public void massExceptionTest() {
        BaseController controller = new BaseController();
        MassException tException = new MassException("test");
        ErrorResponse errorResponse = controller.handleMassException(tException);

        assertEquals("test", errorResponse.getMessage());
    }

    @Test
    @DisplayName("General Exception Test")
    public void generalExceptionTest() {
        BaseController controller = new BaseController();
        MassException tException = new MassException("test");
        ErrorResponse errorResponse = controller.handleException(tException);

        assertEquals("test", errorResponse.getMessage());
    }

}

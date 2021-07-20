package org.hiahatf.mass.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hiahatf.mass.services.rpc.Lightning;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Test class for the utility methods
 */
public class MassUtilTest {

    @Mock
    Lightning lightning;

    private MassUtil util = 
        new MassUtil(0.01, 10000, 10000000, lightning);
    
    @Test
    @DisplayName("Parse Rate Test")
    public void parseRateTest() {
        String data = "{BTC: 0.0076543}";
        Double testRate = util.parseMoneroRate(data);
        Double expectedRate = 0.007730843;
        assertEquals(expectedRate, testRate);
    }

}

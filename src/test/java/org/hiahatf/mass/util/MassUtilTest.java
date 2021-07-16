package org.hiahatf.mass.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for the utility methods
 */
public class MassUtilTest {

    private MassUtil util = new MassUtil(0.01);
    
    @Test
    @DisplayName("Parse Rate Test")
    public void parseRateTest() {
        String data = "{BTC: 0.0076543}";
        Double testRate = util.parseMoneroRate(data);
        Double expectedRate = 0.007730843;
        assertEquals(expectedRate, testRate);
    }

}

package org.hiahatf.mass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class MassApplicationTest {

        @Test
        void contextLoads() {
        }

        @Test
        @DisplayName("Mass Startup Test")
        public void massTest() {
                String[] args = new String[0];
                MassApplication.main(args);
        }
}

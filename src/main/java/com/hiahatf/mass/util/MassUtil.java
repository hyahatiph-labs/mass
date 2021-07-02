package com.hiahatf.mass.util;

import org.springframework.stereotype.Service;

@Service("MassUtil")
public class MassUtil {
    
    public MassUtil() {/* Empty constructor*/}

    /**
     * Hepler method for parsing Monero rate
     * @param rateString
     * @return Monero rate
     */
    public Double splitMoneroRate(String rateString) {
        return Double.valueOf(rateString.split(":")[1].split("}")[0]);
    }

}

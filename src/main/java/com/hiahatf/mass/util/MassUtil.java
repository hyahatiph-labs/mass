package com.hiahatf.mass.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("MassUtil")
public class MassUtil {

    private Logger logger = LoggerFactory.getLogger(MassUtil.class);
    private Double markup;

    /**
     * Mass utility class constructor
     * @param markup
     */
    public MassUtil(@Value("${markup}") Double markup) {
        this.markup = markup;
    }

    /**
     * Hepler method for parsing Monero rate
     * @param rateString
     * @return Monero rate
     */
    public Double parseMoneroRate(String rateString) {
        Double parsedRate = Double.valueOf(rateString.split(":")[1].split("}")[0]);
        Double realRate = (parsedRate * markup) + parsedRate;
        // create the real rate by adding the markup to parsed rate
        logger.info("parsed rate: {} => real rate: {}", parsedRate, realRate);;
        return realRate;
    }

}

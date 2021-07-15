package org.hiahatf.mass.util;

import org.hiahatf.mass.models.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MassUtil {

    private Logger logger = LoggerFactory.getLogger(MassUtil.class);
    private Double markup;

    /**
     * Mass utility class constructor
     * @param markup
     */
    public MassUtil(@Value(Constants.MARKUP) Double markup) {
        this.markup = markup;
    }

    /**
     * Helper method for parsing Monero rate
     * @param rateString
     * @return Monero rate
     */
    public Double parseMoneroRate(String rateString) {
        Double parsedRate = Double
            .valueOf(rateString
            .split(Constants.SEMI_COLON_DELIMITER)[1]
            .split(Constants.RIGHT_BRACKET_DELIMITER)[0]);
        Double realRate = (parsedRate * markup) + parsedRate;
        // create the real rate by adding the markup to parsed rate
        logger.info(Constants.PARSE_RATE_MSG, parsedRate, realRate);;
        return realRate;
    }

}

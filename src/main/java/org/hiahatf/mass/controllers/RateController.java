package org.hiahatf.mass.controllers;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.Rate;
import org.hiahatf.mass.services.rate.RateService;
import org.hiahatf.mass.util.MassUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Class for bypassing quote creation to extract rate data
 */
@RequestMapping
@RestController
public class RateController extends BaseController {
    
    private RateService rateService;
    private MassUtil massUtil;

    @Autowired
    public RateController(RateService rateService, MassUtil massUtil) {
        this.rateService = rateService;
        this.massUtil = massUtil;
    }

    /**
     * Return the most recent rate data with markup
     * @return Mono<Rate>
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(Constants.MASS_RATE_PATH)
    public Mono<Rate> fetchRate() {
        String strRate = rateService.getMoneroRate();
        Rate rate = Rate.builder().rate(massUtil.parseMoneroRate(strRate)).build();
        return Mono.just(rate);
    }

}

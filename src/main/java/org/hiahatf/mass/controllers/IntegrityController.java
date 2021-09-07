package org.hiahatf.mass.controllers;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.Integrity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Hash application source code in the quote, swap and 
 * integrity services. This controller will also return
 * a hash of its own source code. Although this is not a
 * perfect solution to determining if the swap server is
 * a malicious it will deter attempting to fake the server
 * hashes.
 */
@RequestMapping
@RestController
public class IntegrityController extends BaseController {
    
    private Logger logger = LoggerFactory.getLogger(IntegrityController.class);
    
    
    /**
	 * Hash source code
	 * @return Mono<Integrity>
	 */
    @GetMapping(Constants.INTEGRITY_PATH)
    @ResponseStatus(HttpStatus.OK)
	public Mono<Integrity> validateIntegrity() throws IOException {
		return Mono.just(Integrity.builder()
            .integrity(hashIt(Constants.INTEGRITY_SRC))
            .btcQuoteController(hashIt(Constants.BTC_QUOTE_CONTROLLER_SRC))
            .btcQuoteService(hashIt(Constants.BTC_QUOTE_SERVICE_SRC))
            .btcSwapController(hashIt(Constants.BTC_SWAP_CONTROLLER_SRC))
            .btcSwapService(hashIt(Constants.BTC_SWAP_SERVICE_SRC))
            .xmrQuoteController(hashIt(Constants.XMR_QUOTE_CONTROLLER_SRC))
            .xmrQuoteService(hashIt(Constants.XMR_QUOTE_SERVICE_SRC))
            .xmrSwapController(hashIt(Constants.XMR_SWAP_CONTROLLER_SRC))
            .xmrSwapService(hashIt(Constants.XMR_SWAP_SERVICE_SRC))
            .build());
	}

    private String hashIt(String stringPath) throws IOException {
        Path path = FileSystems.getDefault().getPath(stringPath);
        byte[] data = Files.readAllBytes(path);
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(Constants.SHA_256);
        } catch (NoSuchAlgorithmException e) {
            logger.error(Constants.HASH_ERROR, e.getMessage());
        }  
        return Hex.encodeHexString(digest.digest(data));
    }

}

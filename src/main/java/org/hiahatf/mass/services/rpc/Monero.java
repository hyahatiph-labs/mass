package org.hiahatf.mass.services.rpc;

import java.util.List;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.Destination;
import org.hiahatf.mass.models.monero.proof.GetReserveProofParameters;
import org.hiahatf.mass.models.monero.proof.GetReserveProofRequest;
import org.hiahatf.mass.models.monero.proof.GetReserveProofResponse;
import org.hiahatf.mass.models.monero.transfer.TransferParameters;
import org.hiahatf.mass.models.monero.transfer.TransferRequest;
import org.hiahatf.mass.models.monero.transfer.TransferResponse;
import org.hiahatf.mass.models.monero.validate.ValidateAddressParameters;
import org.hiahatf.mass.models.monero.validate.ValidateAddressRequest;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResponse;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.monero.wallet.create.CreateWalletParameters;
import org.hiahatf.mass.models.monero.wallet.create.CreateWalletRequest;
import org.hiahatf.mass.models.monero.wallet.create.CreateWalletResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateParameters;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateRequest;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Class for handling Monero RPC operations
 */
@Service
public class Monero {
    
    private static final double PICONERO = 1.0E12;
    private String moneroHost;

    /**
     * Monero RPC constructor
     * @param host
     */
    public Monero(@Value(Constants.XMR_RPC_PATH) String host) {
        this.moneroHost = host;
    }

    /**
     * Make the Monero validate_address RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param address
     * @return MoneroValidateAddressResponse
     */
    public Mono<ValidateAddressResponse> validateAddress(String address) {
        // build request
        ValidateAddressParameters params = ValidateAddressParameters
            .builder().address(address).build();
        ValidateAddressRequest request = ValidateAddressRequest
            .builder().params(params).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ValidateAddressResponse.class);
    }

    /**
     * Make the Monero transfer RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param value
     * @param address
     * @return Mono<MoneroTransferResponse>
     */
    public Mono<TransferResponse> transfer(String address, Double amount) {
        // build request
        Double piconeroAmt = amount * PICONERO;
        List<Destination> destinations = Lists.newArrayList();
        Destination destination = Destination.builder()
            .address(address).amount(piconeroAmt.longValue()).build();
        destinations.add(destination);
        TransferParameters params = TransferParameters
            .builder().destinations(destinations).build();
        TransferRequest request = TransferRequest
            .builder().params(params).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(TransferResponse.class);
    }

    /**
     * Make the Monero get_reserve_proof RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param value
     * @return Mono<GetReserveProofResponse>
     */
    public Mono<GetReserveProofResponse> getReserveProof(Double amount) {
        // build request
        Double piconeroAmt = amount * PICONERO;
        GetReserveProofParameters parameters = GetReserveProofParameters
            .builder().amount(piconeroAmt.longValue()).build();
        GetReserveProofRequest request = GetReserveProofRequest
            .builder().params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(GetReserveProofResponse.class);
    }

    /**
     * Make the Monero create_wallet RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param value
     * @return Mono<CreateWalletResponse>
     */
    public Mono<CreateWalletResponse> createWallet(String filename) {
        // build request
        CreateWalletParameters parameters = CreateWalletParameters
            .builder().filename(filename).build();
        CreateWalletRequest request = CreateWalletRequest
            .builder().params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(CreateWalletResponse.class);
    }

    /**
     * Make the Monero open/close_wallet RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param value
     * @return Mono<WalletStateResponse>
     */
    public Mono<WalletStateResponse> controlWallet(WalletState state, 
    String filename) {
        // build request
        String method = state == WalletState.OPEN ? 
            Constants.XMR_RPC_OPEN_WALLET :
            Constants.XMR_RPC_CLOSE_WALLET;
        WalletStateParameters parameters = WalletStateParameters
            .builder().filename(filename).build();
        WalletStateRequest request = WalletStateRequest
            .builder()
            .method(method)
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(WalletStateResponse.class);
    }

}

package org.hiahatf.mass.services.rpc;


import java.util.List;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.Destination;
import org.hiahatf.mass.models.monero.balance.BalanceParameters;
import org.hiahatf.mass.models.monero.balance.BalanceRequest;
import org.hiahatf.mass.models.monero.balance.BalanceResponse;
import org.hiahatf.mass.models.monero.multisig.DescribeParameters;
import org.hiahatf.mass.models.monero.multisig.DescribeRequest;
import org.hiahatf.mass.models.monero.multisig.DescribeResponse;
import org.hiahatf.mass.models.monero.multisig.ExportInfoRequest;
import org.hiahatf.mass.models.monero.multisig.ExportInfoResponse;
import org.hiahatf.mass.models.monero.multisig.FinalizeParameters;
import org.hiahatf.mass.models.monero.multisig.FinalizeRequest;
import org.hiahatf.mass.models.monero.multisig.FinalizeResponse;
import org.hiahatf.mass.models.monero.multisig.ImportInfoParameters;
import org.hiahatf.mass.models.monero.multisig.ImportInfoRequest;
import org.hiahatf.mass.models.monero.multisig.ImportInfoResponse;
import org.hiahatf.mass.models.monero.multisig.MakeParameters;
import org.hiahatf.mass.models.monero.multisig.MakeRequest;
import org.hiahatf.mass.models.monero.multisig.MakeResponse;
import org.hiahatf.mass.models.monero.multisig.PrepareRequest;
import org.hiahatf.mass.models.monero.multisig.PrepareResponse;
import org.hiahatf.mass.models.monero.multisig.SignParameters;
import org.hiahatf.mass.models.monero.multisig.SignRequest;
import org.hiahatf.mass.models.monero.multisig.SignResponse;
import org.hiahatf.mass.models.monero.multisig.SubmitParameters;
import org.hiahatf.mass.models.monero.multisig.SubmitRequest;
import org.hiahatf.mass.models.monero.multisig.SubmitResponse;
import org.hiahatf.mass.models.monero.multisig.SweepAllParameters;
import org.hiahatf.mass.models.monero.multisig.SweepAllRequest;
import org.hiahatf.mass.models.monero.multisig.SweepAllResponse;
import org.hiahatf.mass.models.monero.proof.CheckReserveProofParameters;
import org.hiahatf.mass.models.monero.proof.CheckReserveProofRequest;
import org.hiahatf.mass.models.monero.proof.CheckReserveProofResponse;
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
     * @param address
     * @param amount
     * @return Mono<MoneroTransferResponse>
     */
    public Mono<TransferResponse> transfer(String address, Double amount) {
        // build request
        Double piconeroAmt = amount * Constants.PICONERO;
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
     * @param amount
     * @return Mono<GetReserveProofResponse>
     */
    public Mono<GetReserveProofResponse> getReserveProof(Double amount) {
        // build request
        Double piconeroAmt = amount * Constants.PICONERO;
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
     * @param filename
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
     * @param state
     * @param filename
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

    /**
     * Make the Monero prepare_multisig RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @return Mono<PrepareResponse>
     */
    public Mono<PrepareResponse> prepareMultisig() {
        // build request
        PrepareRequest request = PrepareRequest.builder().build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PrepareResponse.class);
    }

    /**
     * Make the Monero make_multisig RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param multisigInfo
     * @return Mono<MakeResponse>
     */
    public Mono<MakeResponse> makeMultisig(List<String> multisigInfo) {
        // build request
        MakeParameters parameters = MakeParameters.builder()
            .multisig_info(multisigInfo).build();
        MakeRequest request = MakeRequest.builder()
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(MakeResponse.class);
    }

    /**
     * Make the Monero finalize_multisig RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param multisigInfo
     * @return Mono<FinalizeResponse>
     */
    public Mono<FinalizeResponse> finalizeMultisig(List<String> multisigInfo) {
        // build request
        FinalizeParameters parameters = FinalizeParameters.builder()
            .multisig_info(multisigInfo).build();
        FinalizeRequest request = FinalizeRequest.builder()
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(FinalizeResponse.class);
    }
    
    /**
     * Make the Monero import_multisig_info RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param info
     * @return Mono<ImportInfoResponse>
     */
    public Mono<ImportInfoResponse> importMultisigInfo(List<String> info) {
        // build request
        ImportInfoParameters parameters = ImportInfoParameters.builder()
            .info(info).build();
        ImportInfoRequest request = ImportInfoRequest.builder()
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ImportInfoResponse.class);
    }

    /**
     * Make the Monero export_multisig_info RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @return Mono<ExportInfoResponse>
     */
    public Mono<ExportInfoResponse> exportMultisigInfo() {
        // build request
        ExportInfoRequest request = ExportInfoRequest.builder().build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ExportInfoResponse.class);
    }

    /**
     * Make the Monero sign_multisig RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param txDataHex
     * @return Mono<SignResponse>
     */
    public Mono<SignResponse> signMultisig(String txDataHex) {
        // build request
        SignParameters parameters = SignParameters.builder()
            .tx_data_hex(txDataHex).build();
        SignRequest request = SignRequest.builder()
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(SignResponse.class);
    }

    /**
     * Make the Monero submit_multisig RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param txDataHex
     * @return Mono<SubmitResponse>
     */
    public Mono<SubmitResponse> submitMultisig(String txDataHex) {
        // build request
        SubmitParameters parameters = SubmitParameters.builder()
            .tx_data_hex(txDataHex).build();
        SubmitRequest request = SubmitRequest.builder()
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(SubmitResponse.class);
    }

    /**
     * Make the Monero describe_transfer RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param mulltisigTxSet
     * @return Mono<DescribeResponse>
     */
    public Mono<DescribeResponse> describeTransfer(String multisigTxSet) {
        // build request
        DescribeParameters parameters = DescribeParameters.builder()
            .multisig_txset(multisigTxSet).build();
        DescribeRequest request = DescribeRequest.builder()
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(DescribeResponse.class);
    }

    /**
     * Make the Monero sweep_all RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param address
     * @return Mono<SweepAllResponse>
     */
    public Mono<SweepAllResponse> sweepAll(String address) {
        // build request
        SweepAllParameters parameters = SweepAllParameters.builder()
            .address(address).build();
        SweepAllRequest request = SweepAllRequest.builder()
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(SweepAllResponse.class);
    }
    
    /**
     * Make the Monero get_balance RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param address
     * @return Mono<SweepAllResponse>
     */
    public Mono<BalanceResponse> getBalance() {
        // build request
        BalanceParameters parameters = BalanceParameters.builder().build();
        BalanceRequest request = BalanceRequest.builder().params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(BalanceResponse.class);
    }
      
    /**
     * Make the Monero check_reserve_proof RPC call.
     * Due to lack of digest authentication support in 
     * Spring WebFlux, run Monero Wallet RPC with the
     * --rpc-disable-login flag.
     * TODO: roll custom digest authentication support
     * @param address
     * @return Mono<SweepAllResponse>
     */
    public Mono<CheckReserveProofResponse> checkReserveProof(String address, String signature) {
        // build request
        CheckReserveProofParameters parameters = CheckReserveProofParameters.builder()
            .address(address).signature(signature).build();
        CheckReserveProofRequest request = CheckReserveProofRequest.builder()
            .params(parameters).build();
        // monero rpc web client
        WebClient client = WebClient.builder().baseUrl(moneroHost).build();
        return client.post()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.JSON_RPC).build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(CheckReserveProofResponse.class);
    }  

}

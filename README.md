# MASS

[![Build](https://github.com/hyahatiph-labs/mass/actions/workflows/build.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/build.yml)
[![Test](https://github.com/hyahatiph-labs/mass/actions/workflows/test.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/test.yml)
[![CodeQL](https://github.com/hyahatiph-labs/mass/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/codeql-analysis.yml)

[POC] [WIP] Java implementation of monero anon swaps server

This is an experimental Spring Boot Application that utilizes [LND hold invoices](https://wiki.ion.radar.tech/tech/research/hodl-invoice)

If successfully settled the equivalent amount in Monero is sent

## Development

1. Run as Spring Boot App from your favorite IDE or do `mvn clean install && java -jar target/mass-x.x.x.jar`
2. The `src/main/resources/application.yml` can be configured as needed.
3. Run bitcoind on [regtest](https://developer.bitcoin.org/examples/testing.html)
4. Setup LND nodes for invoice generation and settling. *[Polar](https://lightningpolar.com/) is a cool tool!
5. Run Monero on [stagenet](https://monerodocs.org/infrastructure/networks/). Get those lovely piconeros at the [Stagenet Faucet](https://melo.tools/faucet/stagenet/)
6. H2 db runs at host/h2-console.
7. Currently working on Bitcoin core 0.21, LND 0.13.x, Fedora 34, Java 11, Maven 3.6 and Monero 0.17.2

NOTE: currently have an issue with Monero digest authentication rpc calls, so use `--disable-rpc-login`

```bash
~/monero-gui-v0.17.2.2/extras/monero-wallet-rpc --rpc-bind-port=18082 --wallet-file=/path/to/wallet --prompt-for-password --disable-rpc-login --daemon-address monero-stagenet.exan.tech:38081 --stagenet
```

## BTC -> XMR API

1. Generate a quote at GET `http://localhost:6789/quote/xmr`
2. Pay the (hold) `invoice` from response. Fund the consensus wallet by sending quote id and output from make_multisig_info
3. Send export_multisig_info and quote id to initialize the swap
4. Use the `quoteId` (aka preimage hash) and preimage to complete swap execution

samples at `./api.http`

health check at GET `http://localhost:6789/health`

source code integrity hashes at GET `http://localhost:6789/integrity`

obtain rate and bypass quote at GET `http://localhost:6789/rate`

integrity response:

```json
{
  "integrity": "93c53eedd4fd32d832a4d4086f42ba8397d2d1690242183c128785a6e8969c7c",
  "btcQuoteController": "1d5b779a64b0ff237624cef9c2a897b21c6496e4eb61a48674d674020f7dbe43",
  "btcSwapController": "c16f9a67df5f4060cddb9c1179f29a228052f11ad0cd839ccbe9fd64bbb6d26a",
  "btcQuoteService": "05adb103051b7025fe95fb0b152680e02c2aa305374bd5711d7984d5d08a803c",
  "btcSwapService": "a3b021dba3f97b8dcb4c81ddede140574c61a1407c9b77aacd0a2d3f6a4c9cbf",
  "xmrQuoteController": "3d1b335f974985871cf77aa1ab90863b051f960e3ead20c08f7e247c43d68b39",
  "xmrSwapController": "95296e96e25331c88bbaee1bf5c08e30bd9adbaf7bbd6ee2bb107d3e87d3a18f",
  "xmrQuoteService": "a2465a8fe8e61ba97e627074d6742c52fa5656d3fa5a31dbe2d9d052758d3a33",
  "xmrSwapService": "30b07e5568355079595f44fde25bc9696d9dd8977bf2b2d105ad54773365e057"
}
```



### Quote Request

`address` - recipient's address to receive xmr

`amount` - amount of monero requested

`multisigInfo` - client's prepare_multisig info (from a newly generated wallet)

`preimageHash` - client must generate a 32-byte array preimage hash and reveal the preimage to complete the swap

#### Sample preimage generation in node.js

```javascript
// import crypto module
const crypto = require("crypto")
// use cryptographically secure random bytes generation
const preimage = crypto.randomBytes(32)
// get the raw bytes from the buffer
const preimageRaw = JSON.parse(JSON.stringify(preimage)).data
// log the preimage raw bytes
console.log(`preimage bytes: ${preimageRaw}`)
// create SHA-256 hash
const hash = crypto.createHash('sha256')
hash.update(preimage);
// log preimage hash raw bytes
console.log(`hash bytes: ${new Uint32Array(hash.copy().digest())}`)
// log hex-encoded preimage
console.log(`hex: ${preimage.toString('hex')}`)
// log hex-encoded preimage hash
console.log(`hash: ${hash.copy().digest('hex')}`);
```

```json
{
    "address": "56fK1PpmCjz5CEiAb8dbpyQZtpJ8s4B2WaXPqyFD2FU9DUobuSDJZztEPeppvAqT2DPWcdp7qtW6KasCbYoWJC7qBcwWrSH",
    "amount": 0.123,
    "multisigInfo": "MultisigV1QUMnWLpohJt1va3RiRZJ5sMGzRuMCRxLu2L2oLdXEU5jSudoWf2L1UTJ89NUTqFqiRLriB2toCzquARxagDXaBZsKEGVSu9suSobN2jR68fgp4c5doHbKsWExZHQ4UkqcBJhd24VxTA4mR2Yw2eDGpeQ1kR8EW6aMdtozYeNkhKmBt94",
    "preimageHash": [9,89,171,169,187,120,6,135,239,136,79,42,77,80,218,62,155,44,141,244,220,100,203,213,201,181,85,16,29,242,129,80]
}
```

### Quote Response
```json
{
  "amount": 0.123,
  "destAddress": "56fK1PpmCjz5CEiAb8dbpyQZtpJ8s4B2WaXPqyFD2FU9DUobuSDJZztEPeppvAqT2DPWcdp7qtW6KasCbYoWJC7qBcwWrSH",
  "invoice": "lnbcrt715930n1ps3scfzpp5p9v6h2dm0qrg0mugfu4y65x686djer05m3jvh4wfk423q80js9gqdq8d4shxuccqzpgxqrpcgsp506z2znrgcdxyrjd5egdae5n8fa5py8f4cf0x427nlqjgpv3067qq9qyyssqyssc3wn3xrgtw04wm8wamxglpge8cmu0qex6rd4c4jhz4cyggvh3mtaw9axf8hjvvtdl98jxjflscqp50tsu96j08xvzfmmc4akuwyqqw3xlja",
  "maxSwapAmt": 10000000,
  "minSwapAmt": 10000,
  "swapMakeMultisigInfo": "MultisigV1apVSuNSr13h2sjc41NM4twGjavqUGuZXtEWWExcTsfrEahkE1AxKWDSggyNAAhMfvmcjjBSmJbRqfWGWjC9QGFAnEY64yFpkb4LCLmFmCiCNRwbJr5pWoCqnp4q7j8xy8m1R5gcw1vrrFVXhNzzKGZ3pq1RTupACz57qzhBbzDu9B6Ks",
  "mediatorMakeMultisigInfo": "MultisigV1HQhvW8ANd9RMG8WqA68iVbAwWd9F7E1zbVSwv2sWcsD5HddsvBDQSamUUND5GDtyknDV55eXJYwxv22Fd4mZz4AKdMYwzFdaiYpYJHZcayxJDGeZ2naTCmb48DeWqQRiCj1Q3KdT24wJKHRJz8EY72fVezJHQU3S4aKMZ3JfKbnts8wq",
  "swapFinalizeMultisigInfo": "MultisigxV1X69iiu6DQF3PcfhhqAEJgCKVA1vTforUs3GPNqN5uDjRU92SauVA1CGGu3HS7FC57WSkCeZn3dxhyMPAgBdnGgLB13fmdEUgYWx8pJt6AzRnYnBVAhkaFtp1BQTCQf1HEV6BDUfenBzFrnmioL9JUCB8FCisEyf6QXYGVW1uUgYxCevGNDzHKhmMJAvb2pYQTHazTdfeS2VXwfQGaVzVWaJZNGQJ",
  "mediatorFinalizeMultisigInfo": "MultisigxV1X69iiu6DQF3PcfhhqAEJgCKVA1vTforUs3GPNqN5uDjRU92SauVA1CGGu3HS7FC57WSkCeZn3dxhyMPAgBdnGgLB13fmdEUgYWx8pJt6AzRnYnBVAhkaFtp1BQTCQf1HEV6BDUfenBzFrnmioL9JUCB8FCisEyf6QXYGVW1uUgYxCevGNDzHKhmMJAvb2pYQTHazTdfeS2VXwfQGaVzVWaJZNGQJ",
  "quoteId": "0959aba9bb780687ef884f2a4d50da3e9b2c8df4dc64cbd5c9b555101df28150",
  "rate": 0.00582063,
  "reserveProof": {
    "signature": "ReserveProofV21AiqQ3aniJq1RHxjRQnD8AT4qqR4iQ2m75csXaJQszdNjEVT5DQSGM2FU9NFKN44ii44xpPx9maxSiEkcowi7aHhCeZe9B8EANcU4mD3FxBAGXHf4vsTFyAz9bZEUgURsQ7g8NR3dNnAP1pRSSWRTgdqMUV4fAWF666bbHbvt21WeH9e54V5p4E1HVMKKB3f41Ea1G5WDFMYecU34Jeic7gsJe4sKDnyp6FSeNnG6VNGuRnpi9jevjTVxPuUcHaW2c3FXrhe9dymviL44VY6Pn8uGVbtG6QfgKtAnR1xEhSqvkxnhLDUPMfyYjphayXXNWvXnPfKYp2YCZjiQ9nbvZcKj3oTEbMvYfGmKEkrN2qKFTq5TVRT74frGjUTDuJSzFAXWWmSPG6ZNMppnZB17rkNuLcTfFr2QtC8G5nD9Fvmei9FoyQAKi8K9SQarua",
    "proofAddress": "54gqcJZAtgzBFnQWEQHec3RoWfmoHqL4H8sASqdQMGshfqdpG1fzT5ddCpz9y4C2MwQkB5GE2o6vUVCGKbokJJa6S6NSatn"
  }
}
```

### Funding Request

POST http://localhost:6789/swap/fund/xmr

```json
{
    "hash": "02a69bb6043d2a4101502efef3da901095c7ea97c0e6bd277b9b7430de8a7b94",
    "makeMultisigInfo": "MultisigxV1SR6MxEnobnw8xYjJ2WCBNh4gQDHvLR1pX7df9xABRHHL1hbcb5A5NLgCxgEcNz61tpeofhcut9o6xWnFyhBpiGzLKfGTBomYYuN8P1ZPgjNpHBaXM52LStyhaAuFp43WAx2HdHKpVj9pXdmdVrhoWdGRNCKURwPYsPnP1idNvmVAxQajYYave3A4r6DYPzTAETac4pLijvR8ixT3kNgW1oj1RGLY"
}
```

### Funding Response

/swap/fund/xmr response: 

```json
{
  "txid": "88546046d697e8c193fa6a85ab9d8374e9c65c10d2fafbc06223a60cf7c5ad2c",
  "swapAddress": "53QeEYRzE8t4PuTu2VDb9DaWbNvgqYhgf3gEYdyWSoQNggyJJwwZd1nQpqzu3dhfZBXocUrtABLZX6xQY5i3fDT9JvCmYEb"
}
```

#### Mediator

* Attempts to sweep funds if no action is taken t+3600s
* Max 3 retries
* src/main/services/monero/Mediator.java


### Initialize the Swap

* NOTE: there appears to be a bug here with n_ouputs = 0 on the first try, so you need some retry logic here. It 'should' work on the retry but if not open an issue.

* CANCELLING? don't send output from export_multisig_info, just send the hash

POST http://localhost:6789/swap/initialize/xmr

```json

{
    "hash": "0959aba9bb780687ef884f2a4d50da3e9b2c8df4dc64cbd5c9b555101df28150",
    "importInfo": "MultiSigInfo..."
}
```

### Initialize Response 

/swap/initialize/xmr response:
```json

{
    "hash": "0959aba9bb780687ef884f2a4d50da3e9b2c8df4dc64cbd5c9b555101df28150",
    "swapExportInfo": "MultiSigInfo...",
    "mediatorExportInfo": "MultiSigInfo..."
}
```

### Swap

POST http://localhost:6789/swap/xmr

```json
{
    "hash": "02a69bb6043d2a4101502efef3da901095c7ea97c0e6bd277b9b7430de8a7b94",
    "preimage": [223,170,234,101,192,203,20,100,57,7,43,236,232,7,121,194,116,73,13,147,124,252,119,126,25,147,65,130,81,233,9,84]
}
```

/swap/xmr response:

lncli

```bash
user@server:~$ lncli -n regtest payinvoice $PAY_REQ
Payment hash: 02a69bb6043d2a4101502efef3da901095c7ea97c0e6bd277b9b7430de8a7b94
Description: mass
Amount (in satoshis): 77382
Fee limit (in satoshis): 77382
Destination: 03e420f400087f0640ee6dfd5b0b589908133c8cf36a737e2d0c3c908661477597
Confirm payment (yes/no): yes
+------------+--------------+--------------+--------------+-----+----------+-----------------+----------------------+
| HTLC_STATE | ATTEMPT_TIME | RESOLVE_TIME | RECEIVER_AMT | FEE | TIMELOCK | CHAN_OUT        | ROUTE                |
+------------+--------------+--------------+--------------+-----+----------+-----------------+----------------------+
| SUCCEEDED  |        0.041 |       33.868 | 77382        | 0   |      792 | 713583046557696 | 03e420f400087f0640ee |
+------------+--------------+--------------+--------------+-----+----------+-----------------+----------------------+
Amount + fee:   77382 + 0 sat
Payment hash:   02a69bb6043d2a4101502efef3da901095c7ea97c0e6bd277b9b7430de8a7b94
Payment status: SUCCEEDED, preimage: xxx
```

```json

{
  "hash": "02a69bb6043d2a4101502efef3da901095c7ea97c0e6bd277b9b7430de8a7b94",
  "multisigTxSet": "Multisig..."
}

```

sign and submit the transaction 

* [sign_multisig](https://web.getmonero.org/resources/developer-guides/wallet-rpc.html#sign_multisig)

* [submit_multisig](https://web.getmonero.org/resources/developer-guides/wallet-rpc.html#submit_multisig)

### Cancel

* At this point it is still possible to cancel the swap by sending the hash
* If export_multisig_info was sent during initialization then client must wait for 7200s timelock


POST http://localhost:6789/swap/cancel/xmr

```json
{
    "hash": "02a69bb6043d2a4101502efef3da901095c7ea97c0e6bd277b9b7430de8a7b94"
}
```

## XMR -> BTC API

1. Send reserve proof along with refund address, amount and prepare multisig info (two newly generated wallets)
2. Send quoteId and output from make multisig info x3, fund the multisig wallet prior to initializing
3. Send quoteId, export multisig x2 info and payment request generated from the preimage hash (< 7200 expiry). Derive the amount of sats to send from the quote. (rate x amount * COIN)
4. Send quoteId and multisigTxSet in exchange for preimage to complete the swap

### Quote Request

GET http://localhost:6789/quote/btc

```json
{
    "amount": 0.123,
    "proofAddress": "54Czx3ETpFZJPeYJC9RPvD9MzTrRjtdM4AYkryvQVD9259CUZ3shG99S53LzWFr5E1jJgd9fqZkzZbVz28N4oCzJBMLjDpj",
    "proofSignature": "ReserveProofV2...",
    "refundAddress": "54Czx3ETpFZJPeYJC9RPvD9MzTrRjtdM4AYkryvQVD9259CUZ3shG99S53LzWFr5E1jJgd9fqZkzZbVz28N4oCzJBMLjDpj",
    "swapMultisigInfos": ["MultisigV1..."]
}
```

### Quote Response

```json

{
  "amount": 0.123,
  "quoteId": "58e778fa4223f30b14c2228edc31f6c1d0a461c12ef7587f34f0aa213ab8bba8",
  "refundAddress": "54Czx3ETpFZJPeYJC9RPvD9MzTrRjtdM4AYkryvQVD9259CUZ3shG99S53LzWFr5E1jJgd9fqZkzZbVz28N4oCzJBMLjDpj",
  "rate": 0.0055731800000000005,
  "minSwapAmt": 10000,
  "maxSwapAmt": 10000000,
  "sendTo": "5BG7NvDurzu1Mnrb5akNfdZwrQK95m9g8JW61mnxbeFTDHHCoRmt8fQPuHjENNBSRKGtssLyH4xkrhbHKoSzquKpUFcgA3C",
  "swapMakeMultisigInfo": "MultisigV1X...",
  "swapFinalizeMultisigInfo": "Multisigx..."
}
```

### Funding Request

POST http://localhost:6789/swap/fund/btc

```json
{
    "hash": "58e778fa4223f30b14c2228edc31f6c1d0a461c12ef7587f34f0aa213ab8bba8",
    "makeMultisigInfos": ["Multisigx...","Multisigx..."]
}
```

### Funding Response

GET http://localhost:6789/quote/btc

```json
{
  "txid": null,
  "swapAddress": "57ZWGGmAqHBSsXgQrDpkSkPv4QQaqvCobG2ET29HbD81ZgoAPCsvpJDMp5tbSRt76XYGaREsjKCEG4y4kEzzr1avK4t15fw"
}
```

### Initialize Request

POST http://localhost:6789/swap/initialize/btc

```json
{
    "hash": "58e778fa4223f30b14c2228edc31f6c1d0a461c12ef7587f34f0aa213ab8bba8",
    "importInfos": ["46895...","48ksfafi..."],
    "paymentRequest": "lnbcrt..."
}
```

### Initialize Response

```json
{
    "hash": "0959aba9bb780687ef884f2a4d50da3e9b2c8df4dc64cbd5c9b555101df28150",
    "swapExportInfo": "MultiSigInfo...",
    "mediatorExportInfo": null
}

```

### Swap Request

POST http://localhost:6789/swap/btc

```json
{
    "hash": "3ac451b7910267ed82d656cd738fdb05994820ecc90a86b1a3a5c4699910ec07",
    "txset": "...long hex..."
}
```
### Swap Response

```json
{
  "preimage": "88d093e1ee8ba149141bd1ee1e0c9a87b2579a201bab3f4435b24b6503ab59de"
}
```

## Tests

MASS uses JUnit5 - [junit-jupiter](https://junit.org/junit5/) framework

Run `mvn clean install` from the root directory

View test coverage with web browser `./target/site/jacoco/index.htm`

![image](https://user-images.githubusercontent.com/13033037/126047819-09fe351a-be62-4bf9-bd5f-cb3580862c6e.png)

## TODOs

See [milestones](https://github.com/hyahatiph-labs/mass/milestones)

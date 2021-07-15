# MASS

[![Build](https://github.com/hyahatiph-labs/mass/actions/workflows/build.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/build.yml)
[![Test](https://github.com/hyahatiph-labs/mass/actions/workflows/test.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/test.yml)
[![CodeQL](https://github.com/hyahatiph-labs/mass/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/codeql-analysis.yml)

[POC] [WIP] Java implementation of monero anon swaps server

This is an experimental Spring Boot Application that utilizes [LND hold invoices](https://wiki.ion.radar.tech/tech/research/hodl-invoice)

If successfully settled the equivalent amount in Monero is sent

## Development

1. Run as Spring Boot App from your favorite IDE or do `mvn clean install && java -jar target/mass-0.0.1-SNAPSHOT.jar`
2. The `src/main/resources/application.yml` can be configured as needed.
3. Run bitcoind on [regtest](https://developer.bitcoin.org/examples/testing.html)
4. Setup LND nodes for invoice generation and settling. *[Polar](https://lightningpolar.com/) is a cool tool!
5. Run Monero on [stagenet](https://monerodocs.org/infrastructure/networks/)
6. H2 db runs at host/h2-console. Execute the `src/main/resources/schema.sql` first
7. Currently working on Bitcoin core 0.21, LND 0.12.x, Debian 10, Java 11, Maven 3.6 and Monero 0.17.2

NOTE: currently have an issue with Monero digest authentication rpc calls, so use `--disable-rpc-login`

```bash
~/monero-gui-v0.17.2.2/extras/monero-wallet-rpc --rpc-bind-port=18082 --wallet-file=/path/to/wallet --prompt-for-password --disable-rpc-login --daemon-address monero-stagenet.exan.tech:38081 --stagenet
```

## API

samples at `./api.http`

health check at GET `http://localhost:6789/health`

generate a quote at GET `http://localhost:6789/quote/xmr`

request:

```json
{
    "amount": 0.0123, 
    "address": "54gqcJZAtgzBFnQWEQHec3RoWfmoHqL4H8sASqdQMGshfqdpG1fzT5ddCpz9y4C2MwQkB5GE2o6vUVCGKbokJJa6S6NSatn"
}
```

response:

```json
{
  "quoteId": "e8754cd5189125b46490b30cb958792e88ae34e76b954d32ad70ced27ac21c2a",
  "address": "54gqcJZAtgzBFnQWEQHec3RoWfmoHqL4H8sASqdQMGshfqdpG1fzT5ddCpz9y4C2MwQkB5GE2o6vUVCGKbokJJa6S6NSatn",
  "isValidAddress": true,
  "amount": 0.0123,
  "rate": 0.006363,
  "invoice": "lnbcrt78260n1psw3ax6pp5ap65e4gcjyjmgeyskvxtjkre96y2ud88dw256v4dwr8dy7kzrs4qdq8d4shxuccqzpgxqzjcsp5wyywgyzhek48wdpwq3jl04jn203d07s9huwpl7dyducstjh2eqcs9qyyssq6j27kf9vzydqqhqaal2cdryzn7u4xgm3vnltvj4qsd9aqhavpwcre5q4sy0megg005gj0zycs3j3l3nvleqqxklppknjgug30sauq8cpe2sm74"
}
```

pay the (hold) `invoice` from response

use the `quoteId` (aka preimage hash) to complete swap execution

execute swap at POST `http://localhost:6789/swap/xmr`

request:

```json
{
    "hash": "e8754cd5189125b46490b30cb958792e88ae34e76b954d32ad70ced27ac21c2a" 
}
```

response:

```json
{
  "hash": "e8754cd5189125b46490b30cb958792e88ae34e76b954d32ad70ced27ac21c2a",
  "txId": "fc30f5dceccc9a5514af8ec6d01e2bd8405282382a973ed8185d8d2ac8a03934"
}
```

## TODOs

Refund / Cancel logic, Tests, etc

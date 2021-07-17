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
    "amount": 0.123, 
    "address": "54gqcJZAtgzBFnQWEQHec3RoWfmoHqL4H8sASqdQMGshfqdpG1fzT5ddCpz9y4C2MwQkB5GE2o6vUVCGKbokJJa6S6NSatn"
}
```

response:

```json
{
  "quoteId": "fbbdbe08b60d66514dfa295f9f192413bb549f217479fabf5ae3887f1ccdc1a2",
  "address": "54gqcJZAtgzBFnQWEQHec3RoWfmoHqL4H8sASqdQMGshfqdpG1fzT5ddCpz9y4C2MwQkB5GE2o6vUVCGKbokJJa6S6NSatn",
  "isValidAddress": true,
  "reserveProof": "ReserveProofV21AhsuJUQ5NQM34GcZLyXsM9FiMFHGWoc52jBZfhbcZ9655hLz7SZjXAcDQLk3QH2P1V31zuUCkeb6Q5CEagUj78TimRphkAAALNZ2Y24mGS5TGXLCYAaZK5ce2Vd2myDvwjYDGx4GruaBK3VkGrKjn5H8VSCDQp2kKyn5acdP7NnVqFLQ6qPmqTCPbFfeB9xzXNjc133bjyG2ybL3UxJ9WmxQJD9e1N8dST9oFijLXVNZRJYy9D9FTUpeQeFa6iYcU9HX9wh6Hgym5USNS7dh9oKZaU8Pehh5ojS7Qk2HMk6dWa38pw6YKuJff9QAS2Ga5hNMHbb1XjjM5MURdHR6N59vHXPkN3xTnmZd2jqgH9f3esX3LnqnKiKe6UVEXVwkCyJxxv2i366TTYSSfxmqEJZcgHhBa7iLN2ucRXbRcBsHEgCiZPftoAWRHX81YH",
  "minSwapAmt": 10000,
  "maxSwapAmt": 10000000,
  "amount": 0.123,
  "rate": 0.00625089,
  "invoice": "lnbcrt768850n1pswlqxppp5u6cx33xa2h8xq457kr2m9zxh86e86zgx6ea2jspdf46awx7yg4psdq8d4shxuccqzpgxqzjcsp5cy8x9tqy0uvg4zfff9nfu0ax3372eetr8yu8xy54dj2f4slyhhrq9qyyssqdwzknhy23e843del966hup2jt3gl89l8a5ztun8cln2dxz82sv6rz7eqh78r6te2d667pxck2l23m05v5ql5ug2xyfdkf7xd9adrp2qps9dy98"
}

```

pay the (hold) `invoice` from response

use the `quoteId` (aka preimage hash) to complete swap execution

execute swap at POST `http://localhost:6789/swap/xmr`

request:

```json
{
    "hash": "fbbdbe08b60d66514dfa295f9f192413bb549f217479fabf5ae3887f1ccdc1a2" 
}
```

response:

```json

{
  "hash": "fbbdbe08b60d66514dfa295f9f192413bb549f217479fabf5ae3887f1ccdc1a2",
  "metadata": "02000102000b8e8ee801a5a31b..."
}

```

relay the transaction with [relay_tx](https://web.getmonero.org/resources/developer-guides/wallet-rpc.html#relay_tx)

## Tests

MASS uses JUnit5 - [junit-jupiter](https://junit.org/junit5/) framework

Run `mvn test` from the root directory

View test coverage with web browser `./target/site/jacoco/index.htm`

![image](https://user-images.githubusercontent.com/13033037/126047819-09fe351a-be62-4bf9-bd5f-cb3580862c6e.png)


## TODOs

See [milestones](https://github.com/hyahatiph-labs/mass/milestones)

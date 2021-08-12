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
5. Run Monero on [stagenet](https://monerodocs.org/infrastructure/networks/)
6. H2 db runs at host/h2-console.
7. Currently working on Bitcoin core 0.21, LND 0.13.x, Fedora 34, Java 11, Maven 3.6 and Monero 0.17.2

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
  "quoteId": "63eb4534535a4c4afa9455f7dacde8cecbbac91e2bcd390407e1b88704a9a758",
  "address": "54gqcJZAtgzBFnQWEQHec3RoWfmoHqL4H8sASqdQMGshfqdpG1fzT5ddCpz9y4C2MwQkB5GE2o6vUVCGKbokJJa6S6NSatn",
  "isValidAddress": true,
  "reserveProof": {
    "signature": "ReserveProofV21AhtWZDjV1SG7AcQFSxfSVWZvQB9QG99kgr2havWLjWgewkBnnKYBt3UqQycx7A9sTaNYfiCo8PLGi28kjP7f9SvN16QNUMNaLKH7kuqySYJ4kYtnPT8qPnHK72weEpQXZhmAm3ebXEjZiH9wskFnVEfVjeCBegqcAVNsXjBZHfv95NZBoE4MgKZvfDT2ank1cqLj7VLUyC4pGVR2Y8bNdv8R1gjjQEQFo6r4YFcPKUz59k6yQ1iokWr6ZJwEauMviEk5CNEK8XYUr47TWJTzM5S3whFW5NhDZFeQ1fdsHTHbV332kwcHoDjGf3ZKaeGa5hNMHbb1XjjM5MURdHR6N59vHXPkN3xTnmZd2k1d6Dg8btwutBZujBBzWT5QNswm1V4ewutYTBBcg1cT8XsZh5MtG7cpobgaHGYYxEtGSfpD9R3agCJBpF5EZ9vsm5",
    "proofAddress": "56fK1PpmCjz5CEiAb8dbpyQZtpJ8s4B2WaXPqyFD2FU9DUobuSDJZztEPeppvAqT2DPWcdp7qtW6KasCbYoWJC7qBcwWrSH"
  },
  "minSwapAmt": 10000,
  "maxSwapAmt": 10000000,
  "amount": 0.123,
  "rate": 0.00629129,
  "invoice": "lnbcrt773820n1ps0xdf8pp5v0452dzntfxy47552hma4n0gem9m4jg790xnjpq8uxugwp9f5avqdq8d4shxuccqzpgxqzjcsp5a75z3kfuwvas78t2a8rmm7j04su4e7t2dwh02x3e0dvwpc6w4urs9qyyssqqqryuthw0sgmtpwymmqjue89ltsre8vnh9uzrey9fs46tynqfk4rxnq5jwyjwvq3vksndfklxa578540zhuu9dprjweyezqjhcg9n8qp068g75"
}
```

pay the (hold) `invoice` from response

use the `quoteId` (aka preimage hash) to complete swap execution

execute swap at POST `http://localhost:6789/swap/xmr`

request:

```json
{
    "hash": "63eb4534535a4c4afa9455f7dacde8cecbbac91e2bcd390407e1b88704a9a758" 
}
```

response:

lncli

```bash
user@server:~$ lncli -n regtest payinvoice $PAY_REQ
Payment hash: 63eb4534535a4c4afa9455f7dacde8cecbbac91e2bcd390407e1b88704a9a758
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
Payment hash:   63eb4534535a4c4afa9455f7dacde8cecbbac91e2bcd390407e1b88704a9a758
Payment status: SUCCEEDED, preimage: cb4605aa339a21d70e20db617a2853214759999cac90c35e5a65fd2462bc0447
```

```json

{
  "hash": "63eb4534535a4c4afa9455f7dacde8cecbbac91e2bcd390407e1b88704a9a758",
  "metadata": "02000102000b8e8ee801a5a31b..."
}

```

relay the transaction with [relay_tx](https://web.getmonero.org/resources/developer-guides/wallet-rpc.html#relay_tx)

## Tests

MASS uses JUnit5 - [junit-jupiter](https://junit.org/junit5/) framework

Run `mvn clean install` from the root directory

View test coverage with web browser `./target/site/jacoco/index.htm`

![image](https://user-images.githubusercontent.com/13033037/126047819-09fe351a-be62-4bf9-bd5f-cb3580862c6e.png)


## TODOs

See [milestones](https://github.com/hyahatiph-labs/mass/milestones)

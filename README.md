# mass

[![CI](https://github.com/hyahatiph-labs/mass/actions/workflows/main.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/main.yml)
[![CodeQL](https://github.com/hyahatiph-labs/mass/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/codeql-analysis.yml)

[POC] [WIP] Java implementation of monero anon swaps server

This is an experimental Spring Boot Application that utilizes [LND hold invoices](https://wiki.ion.radar.tech/tech/research/hodl-invoice)

If successfully settled the equivalent amount in Monero is sent

## Development

1. Run as Spring Boot App from your favorite IDE or do `java -jar MassApplication.java`
2. The application.yml can be configured as needed.
3. Run bitcoind on [regtest](https://developer.bitcoin.org/examples/testing.html)
4. Setup LND nodes for invoice generation and settling. *[Polar](https://lightningpolar.com/) is cool tool!
5. Run Monero on [stagenet](https://monerodocs.org/infrastructure/networks/)
6. H2 db runs at host/h2-console. Execute the schema.sql first

## API

samples at ./api.http

generate a quote at `http://localhost:6789/quote/xmr`

```json
{
    "amount": 0.123, 
    "address": "5xxx"
}
```

pay the (hold) `invoice` from response

use the `quoteId` (aka preimage hash) to complete swap execution

execute swap at `http://localhost:6789/swap/xmr`

```json
{
    "hash": "20uo2ksfnlkf" 
}
```

## TODOs

Refund / Cancel logic, Tests, etc

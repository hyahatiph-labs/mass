# MASS

[![Build](https://github.com/hyahatiph-labs/mass/actions/workflows/build.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/build.yml)
[![Test](https://github.com/hyahatiph-labs/mass/actions/workflows/test.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/test.yml)
[![CodeQL](https://github.com/hyahatiph-labs/mass/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/hyahatiph-labs/mass/actions/workflows/codeql-analysis.yml)

[ POC ] / [ WIP ] Java implementation of monero anon swaps suite

This is an experimental Spring Boot Application that utilizes [LND hold invoices](https://wiki.ion.radar.tech/tech/research/hodl-invoice)

If successfully settled the equivalent amount in Monero or Bitcoin is sent

## MASS - Server Development

### Manual

1. Run as Spring Boot App from your favorite IDE or do `mvn clean install && java -jar target/mass-x.x.x.jar`
2. The `src/main/resources/application.yml` can be configured as needed.
3. Run bitcoind on [regtest](https://developer.bitcoin.org/examples/testing.html)
4. Setup LND nodes for invoice generation and settling. *[Polar](https://lightningpolar.com/) is a cool tool! See [LND Reference API](https://api.lightning.community/#lnd-rest-api-reference) for documentation.
5. Run Monero on two [stagenet](https://monerodocs.org/infrastructure/networks/) servers. Get those lovely piconeros at the [Stagenet Faucet](https://melo.tools/faucet/stagenet/) or by mining.
6. H2 db runs at host/h2-console.
7. Currently working on Bitcoin core 0.21, LND 0.13.x, Fedora 34, Java 11, Maven 3.6 and Monero 0.17.2

NOTE: currently have an issue with Monero digest authentication rpc calls, so use `--disable-rpc-login`

```bash
~/monero-x86_64-linux-gnu-v0.17.2.3/monero-wallet-rpc --rpc-bind-port=18083 --wallet-dir=/home/USER/Monero/wallets/mass/ --disable-rpc-login --stagenet
```

### Docker

see [docker.md](./docker.md)

## API

see [api.md](./api.md)

## Tests

MASS uses JUnit5 - [junit-jupiter](https://junit.org/junit5/) framework

Run `mvn clean install` from the root directory

View test coverage with web browser `./target/site/jacoco/index.htm`

![image](https://user-images.githubusercontent.com/13033037/126047819-09fe351a-be62-4bf9-bd5f-cb3580862c6e.png)

## APM

MASS works well with the [Glowroot](https://glowroot.org/) open-source project
* Extract the zip to your home directory
* Run MASS with `java -javaagent:/home/USER/glowroot/glowroot.jar -jar target/mass-x.x.x-beta.jar`
* Application performance dashboard is located at `http://localhost:4000`

## MASC - Client Development
see client [readme.md](https://github.com/hyahatiph-labs/mass/tree/dev/masc)

## TODOs

See [milestones](https://github.com/hyahatiph-labs/mass/milestones)

## Contributing

* Styling
    * MASS uses prettier pre-commit hooks
    * Install Node.js 10+
    * run `npm install` from the root directory
* Fork and open pr.
* Working code is underrated

## Comms

* support@hiahatf.org
* donations via OpenAlias => hiahatf.org
* [Element Matrix-based messenger](https://matrix.to/#/#hiahatf:matrix.org)

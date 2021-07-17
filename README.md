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
  "metadata": "02000102000b8e8ee801a5a31bbd05ab03a808870bed02ea0417f7042acfb20b3a9682fc15185fbc003f4a7dcb2ca79ff5344bbf3df4fe68d14b2f5a270200020224b1dbee64ff5a4e036c9711446f6e9197f8755fe863cd916080ec4552efa300022f2e782f067ad5737036fcc2786dabbdd7f33413009f27db49242499bd30d5ba2c01c8c99382f8164714a7fb214f9ab44625b54ff7e24e0da158250b79962ed7f232020901f554220a74585c7005c0aca61f591d485077b4dbf85d024c1cb0db094a4fad79260c8fd0b370df8a4287e5d9f503a7f9618d84e134db0a23b093555dcd247b5e884115ac6e25a070401db69fa104183c23b3f22340d5c6aff44f3a2b2301313c652a62787c0b83f7ac254a81d10d772718a8aa6d4d6d16ddd4dc0b906a53100a807fe423e86719d446c71864149d7ba51c5ed0d77d7aaf4fa18ad6687e5adb939f041f9af7de3af1f12d94fff42c0d566c41e41c86754bff11f2b223e4d56c3d320befb5a0ca08b79fa301e21b00bf30ef2801a46b0b16139fbb54cfe5c95ef0e9d944aad981128b695c26a6323c101293ec767d23cd692a83494fb20409b4639b16c4806eb2655b5f17b3f3194749bdabd50bec71eeba8d921e00b16b070768f77408bc1f462a2e384762d20d100c95eaf6385f84d26976ff571fce37a36d8008a7e402e72cb4b1956fffbab6bfc558dba8711f6b5bc5e474f401db55e67891a040e33418a9cdf46887034400885a9d2b33615de5f10e01b9f0a574342e2c5d260ba4cb8568254647c6236c07bd2dbeca65c9a8ba4fae7dd9ddc593f77f9d7f49e4269ddea67f1dd9fa129342dc425b45acc90c68d6e27d34004c910c9590a693307759255a66aa344a7936efaf914a01e8dcb437dc1952e198c9d74804096420ac27485798cfb077ec4bce0f64378ec0a714442d157b92b428409972961d07d4a67f2c4aec3c5a68fca048b571674775b956b4b4cd9df73761d1c6807726bc5f851397841a0518d9c90c162e2a702fd3ea493f58f5f9d1ee0a25a7aca60b7ee5fcf2d454f8f8c1129eae9737d360acbfd57861a5555fc31b0708895f425a5922b485351c6bb357797baf3a0d4016d880d93657b4dd06ddec79e9e5d1a5b149e919aa9ca2188d479cdd64304d3b7aa525030643028d739279ab614fc6c6c2ac512d40f64746790f25159fa44f26897425a173d625261b5d9a77cc999a64edd54c0ca679f4954ee4ec91c5fe283aa3168a896be5d965133f96f57cbe60ba1443204899c5d5c5a86c6ae60e75c5ade21f2765746386a6c6ce1f60ea0d36a823084a332c5506425174e90fe61b24d9af2d8cd8c9a3146e8220cba5b08d97ca310d631020c51a916c9ab3d387ecfd7effb77123534eb9722543ac214b133a5896045f14068de1f30e40c983151c655739ec1c2b9459f3a3210f430106738932a302119381bfc169045e8a9cd357d19ea4411f0601c83b728c76038f90b74a61d0026fe5414c686d882191b892aa92cbd13a996022d9dea1ac56ae6097e8aa6649047f58bf170e48b33ae2ded467b9a837f40b4e211d0ec6d683f624acf595e067034b12463766d8d532e3c9cf86d194ac04084f851e48b2ad140514c39495b4cd047e5ead4d53965fbdfe8796d56ed639024308222a2ce4385fe5bf4b1ccedec8037d64550852cf3613a3f60b06890caca118b6d7770d557a509b43eb4f71b5bf07a2dad502397fa4749a278ab6b6025c094fc013391bea0a0cf4e2a86955d8ad00353a8aad64013d74ae90f7e7c00e5a01ea6345eea4e98adfdb7174c8e15eb80b806e3a8554a587ad51c5a8873abd0c24e08e80f0d9ed88a77ab3dafa92d2e30245c7398f094e8cc1a40368a088118dc19b24bb4f2acbde9d04623912bcdb9703028ddf8b204b7c8ba5e56bcf6d4daaff9b95bbe800d0ec113a76c0fddd97750bbafd16f7a8c23dbe5431c56b21399209c83b9317d2d8844d9661657f16600c69d8f7ceef177b0a7f8b508e527990948890bee54bc54b771c0cf456d5d9746b3e00000000000000004096e903000000000000e0b5bd8a9f98027f77c75d16cfd919129af6ff7773b68ce60db57dfa9d77c86d55f7a0295fc64a9bf9ccc7008da5500e908eb8efd55e869500b3a8fff5c0ca77fc21f5b896105e0000010a433c636662323062336139363832666331353138356662633030336634613764636232636137396666353334346262663364663466653638643134623266356132373e207501ef40a4b7cb4ef352fb0537f0ffc9df49781495adef0e9e0dc257e144150500015f35346771634a5a4174677a42466e5157455148656333526f57666d6f48714c3448387341537164514d477368667164704731667a5435646443707a39793443324d77516b42354745326f3676555643474b626f6b4a4a613653364e5361746e809cfc9aca034b62df40eb5c033d4f55bc3da9ddf49447f02a61fbc0aa2f0561f5fd243e28e8334d5c47e0213cd872f7be3225994a8df5557da88112f1a88fea0b4bc3e67bde0000010b028e8ee80125442e884c00117212235102a060d60dedde52cade6253dc256ac6ebc4c0f1c40c9d355477fe9fadb4d4002d9049705063715cf46d74e82acc2d361540652ef902b3b18302c9587920c8191b6dbaa08f5db90cac24f05419774c09350b523c15138e2617010392ff58746cd998d70c80becea10a173b937acc140124b36bcbedd89353317d02f0b683020ac0e574359c37991cfceafec6743157f3fb3e6cdf66f336ec4effd0cc8a1dfeb43cb0db56ccf9a26425c0aec53bf30ee31a7a020fdb367a92f5791cb5fe63f9029bba8302e2e087017c889c7fb4ad26180be77f05dc4e2f9fede59b91e3945332a3f1c67714a57ad1359452f9c12ce1e9269b10d295545c15985fa8d953700801bd48348b02c3c283026c5a8339c4dbfe5b7af02f4eb0cb4a666f115d49290b9c32223b7a56f400aff966ba0f48e724c27cd6ede9fe0957a09dea806235e61ceb96be6d9d0bb6763b6902cacd830227e0336ce98dcf505a1ee06d1fb5822fc3cbafa933cbdc6dad3662f8b4e76417aef1d8be384b2a8ff88382ff73ffd5a552b1ad63c951fdf2ee96c4d4e7fd6a9a02b7d083025dfe9b49695637d9b741e530f6685db854f8c047dc7fa409806deaebee3cb1a575521ebc13483ede3cf7d3e60c984a342402787c172c398f51645d7047ac7f5a02a1d58302cf3c70006cc4e5d7270c2132d253d24061d1076ae093920b73d54405475b58400e82dad854b0dcd6a8b2b8c4b167dd329f06d6c904ae5dc407db5f03c25d94c602b8d583025771af205f290e860729e780e741aebdc5743a4dc7faf2052b462e154c196aeffb0f9f261862a599de45fa4af11d970721c9206fd879257383044d37127552ac02afda830234dea8a264f469bc8f2cd88ccd71e15c53b68a8f51446ba7af49aa5682feefbbb8bb9164fca886870cb8005da667805eb91dc817d622ad6f107dd71f7fc95cfd02d9da8302edd9f1953744fdd1a83cdce127ed6b00f549c2d0dcf309480d2ef8d67a7aafd2cdf0fd306712444fb2a18074b2ed1e0ad6c42dd159d28880d4116117fa6840600a00000000000000cc372ffe222cdbd5302e993f138cb45458860bce9cb1160bd13fe481515633a400010000000000000020ff9798de080000018fb32ec445541d0e2c855d320d6884ad59864e9b43f740a943a938ab10b7bc05000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0b5bd8a9f98027f77c75d16cfd919129af6ff7773b68ce60db57dfa9d77c86d55f7a0295fc64a9bf9ccc7008da5500e908eb8efd55e869500b3a8fff5c0ca77fc21f5b896105e0000025f35346771634a5a4174677a42466e5157455148656333526f57666d6f48714c3448387341537164514d477368667164704731667a5435646443707a39793443324d77516b42354745326f3676555643474b626f6b4a4a613653364e5361746e809cfc9aca034b62df40eb5c033d4f55bc3da9ddf49447f02a61fbc0aa2f0561f5fd243e28e8334d5c47e0213cd872f7be3225994a8df5557da88112f1a88fea0b4bc3e67bde000000e0b5bd8a9f98027f77c75d16cfd919129af6ff7773b68ce60db57dfa9d77c86d55f7a0295fc64a9bf9ccc7008da5500e908eb8efd55e869500b3a8fff5c0ca77fc21f5b896105e0000010a2c01c8c99382f8164714a7fb214f9ab44625b54ff7e24e0da158250b79962ed7f232020901f554220a74585c70000000000000000001000303015f35346771634a5a4174677a42466e5157455148656333526f57666d6f48714c3448387341537164514d477368667164704731667a5435646443707a39793443324d77516b42354745326f3676555643474b626f6b4a4a613653364e5361746e809cfc9aca034b62df40eb5c033d4f55bc3da9ddf49447f02a61fbc0aa2f0561f5fd243e28e8334d5c47e0213cd872f7be3225994a8df5557da88112f1a88fea0b4bc3e67bde000000000000010000"
}

```

relay the transaction with [relay_tx](https://web.getmonero.org/resources/developer-guides/wallet-rpc.html#relay_tx)

## Tests

MASS uses JUnit5 - [junit-jupiter](https://junit.org/junit5/) framework

Run `mvn test` from the root directory

## TODOs

See [milestones](https://github.com/hyahatiph-labs/mass/milestones)

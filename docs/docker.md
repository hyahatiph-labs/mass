# Docker

* docker file is in the `./devops/` directory
* pull the docker image

```bash
docker pull hiahatf/mass-dev:0.2.1-beta
```

* enter the image

```bash
sudo docker run -it mass:0.2.1-beta /bin/bash
```

* run the initial setup

```bash
cd
./start-mass-dev.sh
```

* Automated wallet setup and funding is pending
* When the console prints `"Dev environment initialized..."` fund the lnd nodes:

 ```bash
export lncli1="/root/lnd-linux-amd64-v0.13.1-beta/lncli -n regtest --lnddir=/root/.lnd-regtest-1"
export lncli2="/root/lnd-linux-amd64-v0.13.1-beta/lncli -n regtest --lnddir=/root/.lnd-regtest-2 --rpcserver=localhost:11009"
$lncli1 create
$lncli2 create
$lncli2 newaddress p2wkh
/root/bitcoin-0.21.1/bin/bitcoin-cli -regtest generatetoaddress 101 <address from above>
$lncli1 getinfo | grep pubkey
$lncli2 connect PUBKEY@127.0.0.1:9735
$lncli2 openchannel PUBKEY AMOUNT
```

* generate 10 more blocks to either wallet for channel confirmation
* verify the with `channelbalance`
* balance the channels with `addinvoice` on node1 and `payinvoice` on node 2
* get addresses for monero with open_wallet (mass-alice or mass-bob) and [get_balance](https://www.getmonero.org/resources/developer-guides/wallet-rpc.html#get_address) rpc call and use the [stagenet faucet](https://melo.tools/faucet/stagenet/) or mining
* mass also uses `addholdinvoice`, `settleinvoice` and `cancelinvoice` for payments
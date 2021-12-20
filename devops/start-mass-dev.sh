#!/bin/bash

# script for starting the mass dev env

# start bitcoin core
echo "Starting mass dev environment..."
echo "Starting bitcoind..."
/root/bitcoin-0.21.1/bin/bitcoind -regtest -daemon -fallbackfee=0.001
echo "Starting lnd node 1..."
/root/lnd-linux-amd64-v0.13.1-beta/lnd --lnddir=/root/.lnd-regtest-1 &
echo "Starting lnd node 2..."
/root/lnd-linux-amd64-v0.13.1-beta/lnd --lnddir=/root/.lnd-regtest-2 &
# start monero-wallet-rpc
echo "Starting monero-wallet-rpc for Alice..."
/root/monero-x86_64-linux-gnu-v0.17.2.3/monero-wallet-rpc --rpc-bind-port=38082 --wallet-dir=/root/Monero/wallets/mass/ --disable-rpc-login --daemon-address hiahatf.org:38081 --stagenet &
sleep 5
echo "Starting monero-wallet-rpc for Bob..."
/root/monero-x86_64-linux-gnu-v0.17.2.3/monero-wallet-rpc --rpc-bind-port=38083 --wallet-dir=/root/Monero/wallets/mass/ --disable-rpc-login --daemon-address hiahatf.org:38081 --stagenet &
echo "Creating wallets..."
curl http://localhost:38082/json_rpc -d '{"jsonrpc":"2.0","id":"0","method":"create_wallet","params":{"filename":"mass-alice","language":"English"}}' -H 'Content-Type: application/json'
sleep 5
curl http://localhost:38083/json_rpc -d '{"jsonrpc":"2.0","id":"0","method":"create_wallet","params":{"filename":"mass-bob","language":"English"}}' -H 'Content-Type: application/json'
sleep 5
echo "Starting mass..."
cd /root/mass-alice/mass && /root/apache-maven-3.6.3/bin/mvn clean install 
java -Dspring.profiles.active=alice -jar target/mass* &
cd /root/mass-bob/mass && /root/apache-maven-3.6.3/bin/mvn clean install
java -jar target/mass* &
sleep 20
echo "Dev environment initialized..."
#!/bin/bash

# Script for creating the release

echo "Enter release version: "
read VERSION
rm *.bz2
rm *.gpg
rm -rf ../target/
rm -rf ../masc/node_modules/
tar -cjf mass-$VERSION-beta.tar.bz2 ../mass/
echo "Enter GPG key email: "
read EMAIL
gpg --sign --default-key $EMAIL mass-$VERSION-beta.tar.bz2
gpg --verify mass-$VERSION-beta.tar.bz2.gpg 
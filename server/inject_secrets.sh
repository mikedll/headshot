#!/usr/bin/env bash

jar -uf ./target/headshot-0.1.0.jar ./secrets.yaml && echo "Successfully injected secrets.yaml into headshot-0.1.0.jar"

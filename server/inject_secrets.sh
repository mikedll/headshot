#!/usr/bin/env bash

jar -uf ./headshot-0.1.0.jar ./BOOT-INF/classes/secrets.yaml && echo "Successfully injected BOOT-INF/classes/secrets.yaml into headshot-0.1.0.jar"

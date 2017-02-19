#!/bin/bash
echo Build started on `date`
apt-get update -y
apt-get install -y
ant all-checks
ls

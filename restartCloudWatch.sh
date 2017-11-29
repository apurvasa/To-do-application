#!/bin/bash

sudo cp /home/ubuntu/awslog.conf /var/awslogs/etc
sudo service awslogs start
sudo service awslogs stop
sudo service awslogs restart
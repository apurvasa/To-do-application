#!/bin/bash

STACK_NAME=$1
echo $STACK_NAME


echo "deleting Security Group"
aws ec2 delete-security-group --group-name csye6225-fall2017-webapp


echo "deleting stack"
aws cloudformation delete-stack --stack-name $STACK_NAME


echo "done"

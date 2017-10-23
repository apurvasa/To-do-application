#!/bin/bash

STACK_NAME=$1
echo $STACK_NAME


echo "deleting stack"
aws cloudformation delete-stack --stack-name $STACK_NAME


echo "done"

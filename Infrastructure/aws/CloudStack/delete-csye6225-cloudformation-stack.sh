#!/bin/bash

STACK_NAME=$1
echo $STACK_NAME


STACK_NAME=$2
echo $BUCKET_NAME

aws s3 rm s3://$BUCKET_NAME --recursive

echo "deleting stack"
aws cloudformation delete-stack --stack-name $STACK_NAME


echo "done"

#!/bin/bash

STACK_NAME=$1
echo $STACK_NAME


INSTANCE_ID=$2
echo $INSTANCE_ID


echo "Changing permission"


aws ec2 modify-instance-attribute --instance-id $INSTANCE_ID --no-disable-api-termination



echo "Terminate Instance"

aws ec2 terminate-instances --instance-ids $INSTANCE_ID



echo "wait for instance to terminate"

 aws ec2 wait instance-terminated  --instance-ids $INSTANCE_ID


echo "deleting Security Group"
aws ec2 delete-security-group --group-name csye6225-fall2017-webapp


echo "deleting stack"
aws cloudformation delete-stack --stack-name $STACK_NAME


echo "done"

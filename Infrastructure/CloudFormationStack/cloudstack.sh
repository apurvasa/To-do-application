#!/bin/bash


AMI="ami-cd0f5cb6"
INSTANCE_TYPE="t2.micro"
SECURITY_GROUP_NAME="csye6225-fall2017-webapp"


echo "Creating a security group"
SECURITY_GROUP=$(aws ec2 create-security-group --group-name csye6225-fall2017-webapp --description "My security group" | grep GroupId)

echo "Authorizing security group access"
aws ec2 authorize-security-group-ingress --group-name csye6225-fall2017-webapp --protocol tcp --port 22 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-name csye6225-fall2017-webapp --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-name csye6225-fall2017-webapp --protocol tcp --port 443 --cidr 0.0.0.0/0


echo $SECURITY_GROUP
export VPC_ID=$(aws ec2 describe-vpcs --query "Vpcs[0].VpcId" --output text)
echo $VPC_ID
export SUBNET_ID=$(aws ec2 describe-subnets --filters "Name=vpc-id, Values=$VPC_ID" --query "Subnets[0].SubnetId" --output text)
echo $SUBNET_ID

echo $SECURITY_GROUP
echo "Launching an instance"
INSTANCE_ID=$(aws ec2 run-instances --image-id $AMI --count 1 --instance-type $INSTANCE_TYPE  --security-groups csye6225-fall2017-webapp  --region us-east-1 --block-device-mappings "[{\"DeviceName\":\"/dev/sdf\",\"Ebs\":{\"VolumeSize\":16,\"VolumeType\":\"gp2\",\"DeleteOnTermination\":false}}]" --disable-api-termination --query "Instances[0].InstanceId" --output text)

echo $INSTANCE_ID

 
 
 
echo "Getting the public IP of the instance"
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID | grep PublicIpAddress | grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}")
echo $PUBLIC_IP
 
 
echo "Configuring Route 53 and pointing it to above IP address"





HOSTED_ZONE_ID=$(aws route53 list-hosted-zones --query HostedZones[0].Id --output text)
ZONE_ID=${HOSTED_ZONE_ID:12}
echo $ZONE_ID

echo "Got the domain name"

#Get Domain Name

echo "Get the domain name"

DOMAIN_NAME=$(aws route53 list-hosted-zones --query HostedZones[0].Name --output text)
echo $DOMAIN_NAME



#Add/Update type A resource record set

echo "Add/Update type A resource record set"

aws route53 change-resource-record-sets --hosted-zone-id $ZONE_ID --change-batch "{
  \"Comment\": \"Upsert record set\",
  \"Changes\": [
    {
      \"Action\": \"UPSERT\",
      \"ResourceRecordSet\": {
        \"Name\": \"ec2.$DOMAIN_NAME\",
        \"Type\": \"A\",
        \"TTL\": 60,
        \"ResourceRecords\": [
          {
            \"Value\": \"$PUBLIC_IP\"
          }
        ]
      }
    }
  ]
}"

echo "Done setting A record set"
echo "{\"AWSTemplateFormatVersion\": \"2010-09-09\",\"Description\": \"Sample CloudFormation Template for CSYE 6225 - Fall 2017\",\"Resources\": {\"EC2Instance\": {\"Type\": \"AWS::EC2::Instance\",\"Properties\": {\"KeyName\":\"asssh\",\"ImageId\": \"ami-cd0f5cb6\",\"InstanceType\": \"t2.micro\",\"DisableApiTermination\":\"True\",\"SecurityGroupIds\": [{\"Fn::GetAtt\": [\"WebServerSecurityGroup\",\"GroupId\"]}],\"BlockDeviceMappings\":[{\"DeviceName\":\"/dev/sda1\",\"Ebs\":{\"VolumeSize\":16,\"VolumeType\":\"gp2\"}}],\"SubnetId\": \""$SUBNET_ID"\"}},\"WebServerSecurityGroup\": {\"Type\": \"AWS::EC2::SecurityGroup\",\"Properties\": {\"GroupDescription\": \"Enable HTTP access via port 80, SSH access via port 22\",\"VpcId\": \""$VPC_ID"\",\"GroupName\":\"csye6225-fall2017-$stackName-webapp\",\"SecurityGroupIngress\": [{\"IpProtocol\": \"tcp\",\"FromPort\": \"80\",\"ToPort\": \"80\",\"CidrIp\": \"0.0.0.0/0\"},{\"IpProtocol\": \"tcp\",\"FromPort\": \"22\",\"ToPort\": \"22\",\"CidrIp\": \"0.0.0.0/0\"},{\"IpProtocol\": \"tcp\",\"FromPort\": \"443\",\"ToPort\": \"443\",\"CidrIp\": \"0.0.0.0/0\"}]}},\"MyDNSRecord\": {\"Type\":\"AWS::Route53::RecordSet\",\"Properties\" : {\"Comment\" : \"DNS name for my instance.\",\"Name\" : \""$DOMAIN_NAME"\",\"HostedZoneId\" : \""$HOSTED_ZONE_ID"\",\"Type\" : \"A\",\"TTL\" : \"900\",\"ResourceRecords\" : [{ \"Fn::GetAtt\" : [ \"EC2Instance\", \"PublicIp\" ]}]}}}}" > /home/apurva/abc.json

aws cloudformation create-stack --stack-name $1 --template-body file:///home/apurva/abc.json

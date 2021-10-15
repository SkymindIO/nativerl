#!/usr/bin/env bash
S3BUCKET=$1
default_file=$2
alternative_file=$3

aws s3api head-object --bucket ${S3BUCKET} --key ${default_file} || not_exist=true
if [ ${not_exist} ]; then
  file=${alternative_file}
else
  file=${default_file}
fi

aws s3 cp s3://${S3BUCKET}/${file} ./
ext="${file#*.}"
if [ ${ext} = "tar.gz" ]; then
    tar -xzf ${file}
    rm ${file}
fi

if [ ${ext} = "zip" ]; then
    unzip ${file}
    rm ${file}
fi

SecureS3-FSClient
=================

Secure S3 file system client

A wrapper over Amazon's S3N/S3 Filesystem, to securely connect to to s3n filesystem.
With the default s3n/s3 URI users are required to provide the secret-key/passcode as part of URI.
Ex: sudo -u theuser hadoop fs -ls s3n://AKIAJWJBBSUXIWD7RMEA:LUnvKWWSeFuInkoOpYX/bJtDj170EovlioOchGwM@inmobi-grid-emr-dev/dir/file

This implementation will expose the credentials to every users who is required to connect to S3 bucket.

With S4 filesystem, users can connect to S3 filesystem without specifying credentials as part of URI.
Ex: sudo -u theuser hadoop fs -ls s4://inmobi-grid-emr-dev/dir/file

It works by looking for .crd file in hadoop's logged in user directory.
In HDFS /user/theuser/inmobi-grid-emr-dev.crd file
The name of the file should be the name of the BUCKET and the credentials are provided in this format inside the file:
<key>:<passcode>

Ex: AKIAJWJBBSUXIWD7RMEA:LUnvKWWSeFuInkoOpYX/bJtDj170EovlioOchGwM

The admin can secure this .crd file by restricting to read access for only authorized users.

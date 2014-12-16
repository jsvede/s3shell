#S3Shell
A command line utility for working with S3 Buckets in AWS. S3Shell providers a file system like experience for
moving interacting with a set of buckets outside of the AWS console. Using S3Shell you do things like:

* Store buckets with their access keys and list them
* Change between buckets
* List bucket contents
* Change between paths on a given bucket
* Get files
* Put files
* Delete files


##S3Shell Uses Cliche
S3Shell uses an excellent library called Cliche that provides the infrastructure for building a clean command line
application in Java. Cliche gives you a collection of annotations that truly simplifies the work of building a 
command line. You can get more info about Cliche here:

https://code.google.com/p/cliche/

I've added one interface to Cliche that allows S3Shell to hook into the commands a user enters for storing 
the command history, otherwise I am using Cliche as it was written. I am grateful to Anton Grigoryev, the author
of Cliche, for his answers on how to use the library.

##Getting S3Shell
You can get the complete command line as executable in the releases/ directory at the root of this project.

##Installing S3Shell
After downloading the release of your choice, you install S3Shell by unzipping the zip file archive. 
This will create a directory named s3shell in whatever location.
```
s3shell/s3shell-1.0.0-dist-all.jar
s3shell/s3shell.sh
s3shell/log4j.xml
```
The jar file is an all inclusive jar and includes all the required dependencies. The s3shell.sh is the script that
starts the shell. The log4.xml helps control logging to the terminal. All 3 are required.

##Starting S3Shell
To start s3shell, simply execute the shell script. Once it has started you should see:

```
S3 Shell
s3sh>
```


You are now in the s3shell!

##Using S3Shell
After the shell starts one of the first things you can do is get a list of commands supported. The help infrastructure
is provided by Cliche. To list the commands, enter ?list:
```
S3 Shell
s3sh> ?list
abbrev	name	params
ls	list	(path)
ls	list	()
pwd	present-working-directory	()
lb	list-buckets	(p1)
lb	list-buckets	()
add	add-bucket	(alias, bucketName, accessKey, secretKey)
rmb	remove-bucket	(bucketAlias)
cb	change-bucket	(bucketAlias)
cd	change-directory	(path)
hist	history	()
get	download-file	(remotePath, localPath)
rm	delete-file	(filePath)
put	put-file	(localFile, remotePath)
```
You can more detailed information about a command by using the ?help command:
```
s3sh> ?help add
Command: add-bucket
Abbrev:  add
Params:  (alias, bucketName, accessKey, secretKey)
Description: Command to add a bucket to the BucketLister
Number of parameters: 4
alias	String	An alias for the bucket name
bucketName	String	The literal value for the bucket name
accessKey	String	The access key for the bucket
secretKey	String	The secret key for the bucket
```
All commands in S3Shell should documented well enough for you to understand how to use them without having them documented
here. If not, please send me a note and I'll fix it.

##Exiting the Shell
Simply type 'exit' and the shell will terminate and return you to the OS command line.

##Supported Operating Systems
Linux and OS X. I don't have a Windows machine so I don't have a system to test it on.

##Command
Cliche has 2 great built-in features for help: ?list and ?help . List shows the commands and their parameters
while Help lets you get more info for a given command.
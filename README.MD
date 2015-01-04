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
You can get the current version of the complete command line as executable in the releases/current directory at the 
root of this project.

##Dependencies
You'll need to have a version of Java installed, 1.7 or higher, in order to use s3shell.

##Installing S3Shell
After downloading the release of your choice, you install S3Shell by unzipping the zip file archive. 
This will create a directory named s3shell in whatever location.
```
s3shell/s3shell-0.5.2-dist-all.jar
s3shell/s3shell.sh
s3shell/s3shell.bat
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
Linux and OS X and Windows (experimental).

##Command
Cliche has 2 great built-in features for help: ?list and ?help . List shows the commands and their parameters
while Help lets you get more info for a given command.

###List Buckets
```
Command: list-buckets
Abbrev:  lb
Params:  (p1)
Description: List all bucket information stored within this s3shell instance
```
Description: Lists the buckets you have added to your instance of S3Shell. The command also takes the optional
argument "-la" which will show the access key and the secret key. Normally it only shows the bucket name and the
bucket alias.

###Add Buckets
```
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
This is way you can manually add a bucket to the list of buckets being managed by this instance. You can also import
buckets via a CSV file using the import command.

###Importing Buckets
```
Command: import-buckets
Abbrev:  import
Params:  (fileName)
Description: Import a set of buckets from a CSV file into this S3Shell instance.
Number of parameters: 1
fileName	String	The fully qualified path and file name of your CSV file from which to read.
```

###Exporting Buckets
```
Command: export-buckets
Abbrev:  export
Params:  (fileName)
Description: Export all buckets from this instance of S3Shell to a CSV file.
Number of parameters: 1
fileName	String	The fully qualified path and file name of the file you wish to write to.
```
###Removing a Bucket
```
Command: remove-bucket
Abbrev:  rmb
Params:  (bucketAlias)
Description: Removes a bucket from the list of buckets in Blister
Number of parameters: 1
bucketAlias	String	The alias by which the bucket is referenced
```

### Switching Buckets
```
Command: change-bucket
Abbrev:  cb
Params:  (bucketAlias)
Description: Use the bucket alias that the user passes in.
Number of parameters: 1
bucketAlias	String	sets the current bucket to the one associated with the passed in alias.
```
The change-bucket command allows you to treat each bucket as a root directory in a file system. By changing buckets you
are changing the context that is used for other commands.

### Retrieve a Remote File
```
Command: download-file
Abbrev:  get
Params:  (remotePath, localPath)
Description: Retrieve a file from the specified remote path and write it to the specified local path.
Number of parameters: 2
remotePath	String	The path on the current bucket you want to retrieve
localPath	String	The fully qualified name for the local file. The remote file name is appended to the localFile path
```
In this case the paths are both fully qualified and include the name. 

### Upload a Local File 
```
Command: put-file
Abbrev:  put
Params:  (localFile, remotePath)
Description: Put a file on the current bucket.
Number of parameters: 2
localFile	String	The fully qualified path of a local file for upload.
remotePath	String	The remote file path to upload the file to.
```
As with 'get' this command also expects fully qualifed names.

### Delete A File
```
Abbrev:  rm
Params:  (filePath)
Description: Remove the specified file from current bucket
Number of parameters: 1
filePath	String	The path on the bucket to delete.
```

### Changing Directories
```
Command: change-directory
Abbrev:  cd
Params:  (path)
Description: Change the directories to the specified directory in the path argument
Number of parameters: 1
path	String	For the specified bucket, list the path passed in
```
This command allows you to set a directory for the list command to use while showing directory contents. Directories
don't existing as they do on a filesystem. In S3 they are simply part of the file name; the purpose of this command is
to simplify the relationship between the whole file name and the perceived pathing on the bucket.

### Listing Directories or Files
```
Command: list
Abbrev:  ls
Params:  (path)
Description: For the bucket that the user selected, list the path that is passed in
Number of parameters: 1
path	String	For the specified bucket, list the path passed in

Command: list
Abbrev:  ls
Params:  ()
Description: For the bucket that the user selected, list the path that would be listed in pwd
No parameters.
```
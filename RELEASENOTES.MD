#Release Notes By Version

## 0.8.0
Removed support for MongoDB and CSVs and instead converted to storing buckets and histories in files in user home directory.

## 0.7.0
Added support for MongoDB storage of buckets and history.

Converted to Spring Boot 2.0.

## 0.6.0
Fixed https://github.com/jsvede/s3shell/issues/3 - paging large listing from the ls command.
Added ! bang command to re-run history.
Added experimental s3shell.bat script for using s3shell on windows.
Added asg.cliche to s3shell code base because it's not in maven central and I modified cliche to allow
for the command history and command re-processing.
Updated zip file so that s3shell unpacks into a directory named s3shell/ instead of dist/.

## 0.5.2
Added new feature for re-running a command from history using a bang (!).
Added release notes page.

## 0.5.1
Bux fig for issue https://github.com/jsvede/s3shell/issues/2 .

## 0.5.0
Initial release.
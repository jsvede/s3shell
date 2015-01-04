#Release Notes By Version

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
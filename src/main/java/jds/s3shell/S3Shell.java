/*
 * Copyright (c) 2014, Jon Svede
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package jds.s3shell;


import asg.cliche.CLIException;
import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import asg.cliche.asg.cliche.ext.ShellCommandHandler;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jds.s3shell.entities.Bucket;
import jds.s3shell.util.DownloadProgress;
import jds.s3shell.util.StringPaddingUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The main class for running the shell.
 *
 * @author jsvede
 */
@ComponentScan("asg.cliche")
@ComponentScan("jds.s3shell")
@SpringBootApplication
public class S3Shell implements ShellCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(S3Shell.class);

    private final static String BUCKET_STORAGE_FILE_NAME = "buckets.s3sh";
    private final static String COMMAND_STORAGE_FILE_NAME = "commands.s3sh";


    @Autowired
    private DownloadProgress downloadProgress;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Gson gson;

    private Bucket selectedBucket;

    private String presentWorkingDirectory;

    private AmazonS3Client s3client = null;

    private  static Shell shell;

    private Map<String, Bucket> buckets = new HashMap<>();
    private List<String> commandHistory = new ArrayList<>();

    private String s3HomeDirectory;


    public S3Shell() {
        String userHomeDir = System.getProperty("user.home");
        s3HomeDirectory = userHomeDir + File.separator + ".s3shell";

        final File s3ShellDir = new File(s3HomeDirectory);

        if(!s3ShellDir.exists()) {
            s3ShellDir.mkdir();
        }

        final File bucketFile = new File(s3ShellDir.getPath() + File.separator + BUCKET_STORAGE_FILE_NAME);

        if(bucketFile.exists()) {
           loadBucketFile();
        }

        final File historyFile = new File(s3ShellDir.getPath() + File.separator + COMMAND_STORAGE_FILE_NAME);

        if(historyFile.exists()) {
            loadHistoryFile();
        }

    }

    private void loadBucketFile() {

        try {
            FileReader reader = new FileReader(s3HomeDirectory + File.separator + BUCKET_STORAGE_FILE_NAME);

            GsonBuilder builder = new GsonBuilder();

            // create local builder because the member variable isn't initialized at this time.
            Gson myGson = builder.create();

            Bucket[] bucketsFromFile = myGson.fromJson(reader, Bucket[].class);

            for(Bucket b: bucketsFromFile) {
                buckets.put(b.getAlias(), b);
            }

            reader.close();
        } catch(IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        }
    }

    private void loadHistoryFile() {

        try {
            FileReader reader = new FileReader(s3HomeDirectory + File.separator + COMMAND_STORAGE_FILE_NAME);

            GsonBuilder builder = new GsonBuilder();

            // create local builder because the member variable isn't initialized at this time.
            Gson myGson = builder.create();

            String[] commandsFromFile = myGson.fromJson(reader, String[].class);

            if(commandsFromFile != null && commandsFromFile.length > 0) {

                for(String command: commandsFromFile) {
                    commandHistory.add(command);
                }
            }

            reader.close();
        } catch(IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        }
    }

    public static void main(String[] args) throws IOException {

        SpringApplication.run(S3Shell.class, args);
    }

    @Bean
    public List<String> getList() {
        return new ArrayList<String>();
    }

    @Bean
    public Boolean getBoolean() {
        return new Boolean(false);
    }
    @Bean
    public DownloadProgress getDownloadProgress() {
        return downloadProgress;
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public CommandLineRunner runner(){
        return args -> {

            shell = ShellFactory.createConsoleShell("s3sh", "S3 Shell - v" + version(), context.getBean(S3Shell.class));
            shell.commandLoop();

        };
    }

    @Command(description = "List all bucket information stored within this s3shell instance",
            abbrev="lb")
    public String listBuckets() {

        return listBuckets(null);
    }

    /* TODO: need to think about having users or leveraging user info from the outer
     * shell to allow for more than 1 user for a given install.
     */
    @Command(description = "List all bucket information stored within this s3shell instance",
             abbrev="lb")
    public String listBuckets(String flags) {

        StringBuilder sb = new StringBuilder();
        //List<Bucket> buckets = bucketRepo.findAll();
//        Iterator<Bucket> bucketsIt = buckets.iterator();

        /* This is a pretty hackish way of getting a table but it was easier than other things.
         * Need to refactor this to be less literal and more flexible.
         */
        sb.append("+-").append(StringPaddingUtil.pad("-", 33, false, "-")).append("-+-")
                .append(StringPaddingUtil.pad("-", 63, false, "-")).append("-+");
        if(flags != null && flags.startsWith("-l")) {
            sb.append(StringPaddingUtil.pad("-", 22, false, "-")).append("-+-")
                    .append(StringPaddingUtil.pad("-",40, false, "-")).append("-+").append("\n");
        } else {
            sb.append("\n");
        }

        sb.append("| ").append(StringPaddingUtil.pad("Bucket Alias", 33, false, " ")).append(" | ")
                .append(StringPaddingUtil.pad("Bucket Name", 63, false, " "));
        if(flags != null && flags.startsWith("-l")) {
            sb.append(" | ").append(StringPaddingUtil.pad("Access Key", 21, false, " ")).append(" | ")
                    .append(StringPaddingUtil.pad("Secret Key",40, false, " ")).append(" |").append("\n");

        } else {
            sb.append(" |\n");
        }

        sb.append("+-").append(StringPaddingUtil.pad("-", 33, false, "-")).append("-+-")
                .append(StringPaddingUtil.pad("-", 63, false, "-")).append("-+");

        if(flags != null && flags.startsWith("-l")) {

            sb.append(StringPaddingUtil.pad("--", 22, false, "-")).append("-+-")
                    .append(StringPaddingUtil.pad("-",40, false, "-")).append("-+").append("\n");
        } else {
            sb.append("\n");
        }

        for(Bucket b : buckets.values()) {

            sb.append("| ");
            sb.append(StringPaddingUtil.pad(b.getAlias(), 33)).append(" | ").append(StringPaddingUtil.pad(b.getBucketName(), 63)).append(" | ");
            if(flags != null && flags.startsWith("-l")) {
                sb.append(StringPaddingUtil.pad(b.getAccessKey(), 21, true)).append(" | ")
                        .append(StringPaddingUtil.pad(b.getSecretKey(),40, true)).append(" |");

            }
            sb.append("\n");
        }
        sb.append("+-").append(StringPaddingUtil.pad("-", 33, false, "-")).append("-+-")
                .append(StringPaddingUtil.pad("-", 63, false, "-")).append("-+");
        if(flags != null && flags.startsWith("-l")) {
            sb.append(StringPaddingUtil.pad("-", 22, false, "-")).append("-+-")
                    .append(StringPaddingUtil.pad("-",40, false, "-")).append("-+");
        } else {
            sb.append("\n");
        }

        return sb.toString();
    }

    @Command(description = "Command to add a bucket to the BucketLister",
             abbrev="add")
    public String addBucket(@Param(name = "alias",
                                 description = "An alias for the bucket name")String alias,
                          @Param(name = "bucketName",
                                  description = "The literal value for the bucket name")String bucketName,
                          @Param(name = "accessKey",
                                  description = "The access key for the bucket")String accessKey,
                          @Param(name = "secretKey",
                                  description = "The secret key for the bucket")String secretKey) {


        if(alias != null && alias.length() > 49) {

        }
        Bucket aBucket = new Bucket(alias,bucketName,accessKey,secretKey);
        if(!buckets.containsKey(aBucket.getAlias())) {
            buckets.put(aBucket.getAlias(), aBucket);
        }
        saveBucketsToFile(new ArrayList<Bucket>(buckets.values()), null);
        System.out.println( "bucket count: " + buckets.size());

        return "Added bucket " + bucketName + " with an alias of " + alias;


    }

    private void saveBucketsToFile(List<Bucket> buckets, String fileName) {

        try {
            FileWriter writer;

            if (fileName != null && fileName.length() > 0) {
                writer = new FileWriter(fileName);
            } else {
                writer = new FileWriter(s3HomeDirectory + File.separator + BUCKET_STORAGE_FILE_NAME);
            }
            Type listType = new TypeToken<List<Bucket>>() {}.getType();
            gson.toJson(buckets, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void saveCommandsToFile(List<String> commands) {

        try {
            FileWriter writer = new FileWriter(s3HomeDirectory + File.separator + COMMAND_STORAGE_FILE_NAME);
            Type listType = new TypeToken<List<String>>() {}.getType();
            gson.toJson(commands, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


    @Command(description = "Removes a bucket from the list of buckets in Blister",
             abbrev="rmb")
    public String removeBucket(@Param(name = "bucketAlias",
                                      description = "The alias by which the bucket is referenced")String bucketAlias) {

        StringBuilder sb = new StringBuilder();
        if(buckets.containsKey(bucketAlias)) {
            buckets.remove(bucketAlias);
            sb.append("bucket named " + bucketAlias + " has been deleted");
        } else {
            sb.append("no bucket named " + bucketAlias + " found");
        }
        return sb.toString();
    }

    @Command(description = "For the bucket that the user has selected, list base paths.",
            abbrev="lp")
    public void listPaths() {
        listPaths("");
    }

    @Command(description = "For the bucket that the user has selected, list the paths based on the entry.",
             abbrev="lp")
    public void listPaths(@Param(name = "path",
            description = "will use this value passed to find child paths.")final String path) {

        final String pwd = presentWorkingDirectory;

        if (s3client != null) {

            ListObjectsRequest listObjectRequest = new ListObjectsRequest();

            listObjectRequest.withBucketName(selectedBucket.getBucketName()).withPrefix(path).withDelimiter("/");

            final ObjectListing listing = s3client.listObjects(listObjectRequest);
            final List<String> prefixes = listing.getCommonPrefixes();
            for(String prefix : prefixes) {
                System.out.println(prefix);
            }
            System.out.println("found " + prefixes.size() + " prefixes");

        }
    }

    @Command(description = "Use the bucket alias that the user passes in.")
    public String changeBucket(@Param(name = "bucketAlias",
            description = "sets the current bucket to the one associated with the passed in alias.")String bucketAlias) {

        StringBuilder sb = new StringBuilder();
//        List<Bucket> buckets = buckets.get//bucketRepo.findByAlias(bucketAlias);

        selectedBucket = buckets.get(bucketAlias);
        if(selectedBucket != null) {
            AWSCredentials awsCredentials = new BasicAWSCredentials(selectedBucket.getAccessKey(), selectedBucket.getSecretKey());
            s3client = new AmazonS3Client(awsCredentials, new ClientConfiguration());
            presentWorkingDirectory = "/";
            sb.append("Current bucket is now " + selectedBucket.getAlias());
        } else {
            sb.append("No bucket with alias " + bucketAlias + " found");
        }

        return sb.toString();
    }

    @Command(description = "For the bucket that the user selected, list the path that is passed in",
             abbrev="ls")
    public void list(@Param(name = "path",
                            description = "For the specified bucket, list the path passed in")String path) {
        if(s3client != null) {
            if(path == null || path.length() ==0) {
                path = presentWorkingDirectory;
            }
            if(path != null && path.equals("/")) {
                path = "";
            }

            int totalFileCount = 0;

            ObjectListing listing = s3client.listObjects(selectedBucket.getBucketName(), path);
            List<S3ObjectSummary> summaries = listing.getObjectSummaries();
            while(summaries.size() > 0) {
                totalFileCount = totalFileCount + summaries.size();
                for(S3ObjectSummary summary : summaries) {
                    System.out.println(summary.getLastModified().toString() + " - "+ FileUtils.byteCountToDisplaySize(summary.getSize())+ " - " + summary.getKey().replaceAll("_\\$folder\\$", "/"));
                }
                listing = s3client.listNextBatchOfObjects(listing);
                summaries = listing.getObjectSummaries();
            }

            System.out.println("Items found: " + totalFileCount);
        }
    }

    @Command(description="A command for searching your current path for files using regex",
             abbrev = "f")
    public void find(@Param(name="regexPattern",
                            description="The regex pattern to apply to the file names in the current path")
                     String regexPattern) {

        Pattern pattern = Pattern.compile(regexPattern);

        String path =null;

        if(s3client != null) {
            if(path == null || path.length() == 0) {
                path = presentWorkingDirectory;
            }
            if(path != null && path.equals("/")) {
                path = "";
            }

            long matchCount = 0;

            boolean continueListingData = true;

            ObjectListing listing = s3client.listObjects(selectedBucket.getBucketName(), path);

            long totalFiles = listing.getObjectSummaries().size();

            do {
                listing = s3client.listNextBatchOfObjects(listing);
                for(S3ObjectSummary summary : listing.getObjectSummaries()) {
                    Matcher matcher = pattern.matcher(summary.getKey());
                    while(matcher.find()) {
                        System.out.println(summary.getLastModified().toString() + " - "+ FileUtils.byteCountToDisplaySize(summary.getSize())+ " - " + summary.getKey().replaceAll("_\\$folder\\$", "/"));
                        matchCount++;
                    }
                }

                totalFiles = totalFiles + listing.getObjectSummaries().size();

                if(listing.getObjectSummaries().size() < 1000) {
                    continueListingData = false;
                }
            }while(continueListingData);
            System.out.println("Listed " + matchCount + " of " + totalFiles + " files");
        }
    }

    @Command(description = "For the bucket that the user selected, list the path that would be listed in pwd",
            abbrev="ls")
    public void list() {

        list("");
    }

    @Command(description = "Change the directories to the specified directory in the path argument",
             abbrev="cd")
    public void changeDirectory(@Param(name = "path",
            description = "For the specified bucket, list the path passed in")String path) {

        if(path.startsWith("/")) {
            path = path.substring(1);
            System.out.println("removing leading '/' because it is unnecessary and is interpreted as a literal path");
        }
        if(path.endsWith("/")) {
            presentWorkingDirectory = path;
        } else {
            // TODO - if the user puts a file as the pwd this will cause issues
            presentWorkingDirectory = path + "/";
        }
    }

    @Command(description = "Show which directory the cursor is pointing to",
             abbrev="pwd")
    public String presentWorkingDirectory() {

        if(selectedBucket == null) {
            return "no bucket selected; use changeBucket(cb)";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(selectedBucket.getBucketName());
        if(presentWorkingDirectory != null) {
            if(presentWorkingDirectory.equals("/")) {
                sb.append(presentWorkingDirectory);
            } else if(!presentWorkingDirectory.startsWith("/")){
                sb.append("/").append(presentWorkingDirectory);
            } else {
                sb.append(presentWorkingDirectory);
            }
        }
        return sb.toString();
    }

    @Command(description = "Show the command history for this s3shell shell",
             abbrev="hist")
    public String history() {

        int count = 1;

        StringBuilder sb = new StringBuilder();
        for(String command : commandHistory) {
            sb.append(count).append("  ").append(command).append("\n");
            count++;
        }
        return sb.toString();
    }


    @Command(description = "Retrieve a file from the specified remote path and write it to the specified local path.",
            abbrev = "get")
    public String downloadFile(@Param(name="remotePath",
            description="The path on the current bucket you want to retrieve")
                               String remotePath) {
        String localPath = System.getProperty("user.dir");
        int index = remotePath.lastIndexOf("/");
        String remoteShortFileName = remotePath.substring(index);

        System.out.println("going to use the current working directory of: " + localPath);

        return downloadFile(remotePath, localPath + remoteShortFileName);
    }

    @Command(description = "Retrieve a file from the specified remote path and write it to the specified local path.",
             abbrev = "get")
    public String downloadFile(@Param(name="remotePath",
                                      description="The path on the current bucket you want to retrieve")
                               String remotePath,
                               @Param(name="localPath",
                                      description="The fully qualified name for the local file. The remote file name" +
                                       " is appended to the localFile path")
                               String localPath) {

        ObjectListing listing = s3client.listObjects(selectedBucket.getBucketName(), remotePath);

        if(listing.getObjectSummaries() != null && listing.getObjectSummaries().size() > 1) {
            return "Remote path " + remotePath + " is not unique; found " + listing.getObjectSummaries().size() +
                   " matching that path. Please select a unique file name to copy.";
        } else if((listing.getObjectSummaries() == null) || listing.getObjectSummaries() != null && listing.getObjectSummaries().size() == 0) {
            return "Remote path " + remotePath + " returns zero files.";
        }

        S3ObjectSummary summary = listing.getObjectSummaries().get(0);

        S3Object s3object = s3client.getObject(new GetObjectRequest(selectedBucket.getBucketName(), summary.getKey()));

        InputStream is = s3object.getObjectContent();
        long contentLength = s3object.getObjectMetadata().getContentLength();
        boolean success = false;
        try {
            success = downloadProgress.readFile(is, localPath, contentLength, 1024, 64, System.out);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return "failed to get the remote file " + remotePath + " with error: " + e.getMessage();
        }

        return (success == true ? "Successfully":"Unsuccessfully") + " downloaded remote file " + remotePath;
    }

    @Command(description = "Remove the specified file from current bucket",
             abbrev="rm")
    public String deleteFile(@Param(name = "filePath",
                                    description="The path on the bucket to delete.")String filePath) {

        StringBuilder sb = new StringBuilder();
        if(filePath != null && filePath.endsWith("*")) {
            String basePath = filePath.substring(0, filePath.lastIndexOf("*")-1);
            ObjectListing listing = s3client.listObjects(selectedBucket.getBucketName(), basePath);
            for(S3ObjectSummary summary : listing.getObjectSummaries()) {
                s3client.deleteObject(selectedBucket.getBucketName(), summary.getKey());
                sb.append("Deleted " + selectedBucket.getBucketName() + "://" + summary.getKey()).append("\n");
            }
        } else {
            s3client.deleteObject(selectedBucket.getBucketName(), filePath);
            sb.append("deleted " + filePath + "\n");
        }

        return sb.toString();
    }

    @Command(description = "Put a file on the current bucket.",
             abbrev = "put")
    public String putFile(@Param(name = "localFile",
                                 description = "The fully qualified path of a local file for upload.")String localFile,
                          @Param(name = "remotePath",
                                 description = "The remote file path to upload the file to.")String remotePath) {

        //TODO: add argument validation; NPEs are possible here though I think cliche prevents this.
        File fileForUpload = new File(localFile);
        if(remotePath.startsWith("/")) {
            remotePath = remotePath.substring(1);
            System.out.println("removing leading '/' because it is unnecessary and is interpreted as a literal path");
        }
        if(remotePath.endsWith(".")) {
            String shortFileName = fileForUpload.getName();
            String remoteFilePathStr = remotePath.substring(0, remotePath.length()-1);
            remotePath = remoteFilePathStr + "/" + shortFileName;
        }
        PutObjectResult result = s3client.putObject(new PutObjectRequest(selectedBucket.getBucketName(), remotePath, fileForUpload));
        String contentMd5String = result.getContentMd5();
        System.out.println("md5: " + contentMd5String);

        return "Successfully uploaded " + remotePath;
    }

    @Command(description = "Import a set of buckets from a CSV file into this S3Shell instance.",
             abbrev = "import")
    public String importBuckets(@Param(name = "fileName",
                                       description = "The fully qualified path and file name of your CSV file " +
                                                     "from which to read.")
                                String fileName) {

        File inputFile = new File(fileName);
        if(!inputFile.exists()) {
            return "cannot find file named " + fileName;
        }
        try {
            Reader reader = new FileReader(inputFile);

            try {

                GsonBuilder builder = new GsonBuilder();

                // create local builder because the member variable isn't initialized at this time.
                Gson myGson = builder.create();

                Bucket[] bucketsFromFile = myGson.fromJson(reader, Bucket[].class);

                for(Bucket b: bucketsFromFile) {
                    buckets.put(b.getAlias(), b);
                }

                reader.close();
            } catch(IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            return "Unable to process the bucket information in " + fileName + " due to " + e.getMessage();
        }

        return "";
    }

    @Command(description = "Export all buckets from this instance of S3Shell to a CSV file.",
             abbrev="export")
    public String exportBuckets(@Param(name = "fileName",
                                       description = "The fully qualified path and file name of the " +
                                                     "file you wish to write to.")
                                String fileName) {

        return exportSelectedBuckets(fileName, null);
    }

    @Command(description = "Export selected buckets by name. Pass in a destination file name and a comma " +
                            "separated list of bucket aliases and that data will be written to the specified file")
    public String exportSelectedBuckets(@Param(name = "fileName",
                                               description = "The fully qualified path and file name of the " +
                                                             "file you wish to write to.")
                                        String fileName,
                                        @Param(name = "bucketNames",
                                               description = "A comma separated list of bucket aliases to export.")
                                        String bucketNames) {

        List<Bucket>bucketsForExport = new ArrayList<Bucket>();

        if(bucketNames == null) {
//            Iterable<Bucket> buckets = bucketRepo.findAll();
            for(Bucket b : buckets.values()) {
                bucketsForExport.add(b);
            }
        } else {
            String[] specifiedBuckets = bucketNames.split(",");
            for(String bucketName : specifiedBuckets) {
                Bucket foundBucket = buckets.get(bucketName);
                if(foundBucket != null ) {
                    bucketsForExport.add(foundBucket);
                }
            }
        }

        if(fileName != null) {
            File bucketCsvFile = new File(fileName);
            if(!bucketCsvFile.getParentFile().exists()) {
                boolean createDirs = bucketCsvFile.getParentFile().mkdirs();
                if(!createDirs) {
                    return "unable to create missing parent directories for " + fileName;
                }
            }
            saveBucketsToFile(bucketsForExport, bucketCsvFile.getPath());
            return "wrote " + bucketsForExport.size() + " to file named " + fileName ;
        } else {
            return "fileName parameter cannot be null";
        }
    }

    @Command(description = "A command that shows the version of this instance of S3Shell")
    public String version() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/maven/jds.s3shell/s3shell/pom.properties");
        Properties props = new Properties();
        if(is != null) {
            try {
                props.load(is);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return "Unable to return version information";
            }
            String version = props.getProperty("version");
            return "Version: " + version == null ? "N/A" : version;
        }
        return "No version information available";
    }

    /**
     * Implementation of a method that allows this class to capture command history.
     *
     * @param cmdLine
     * @throws CLIException
     */

//    @Override
//    public void processCommand(String cmdLine) throws CLIException {
//
//    }
    @Override
    public void processCommand(String cmdLine) throws CLIException {

        if(cmdLine != null && cmdLine.startsWith("!")) {
            String[] commandParts = cmdLine.split(" ");
            String commandValue = commandParts[1].trim();
            commandHistory.add(commandValue);
            return;
        }
        commandHistory.add(cmdLine);


        saveCommandsToFile(commandHistory);

    }


    @Command(abbrev="!",
             description = "The command to re-execute a given command in history")
    public void bang(@Param(name = "commandHistory",
                            description = "The command number to re-excute.") Integer commandNumber) {

        String command = commandHistory.get(commandNumber-1);

        try {
            shell.processLine(command);
        } catch (CLIException e) {
            e.printStackTrace();
        }
    }

    public void cliEnterLoop() {

    }

    public void cliLeaveLoop() {

    }

}

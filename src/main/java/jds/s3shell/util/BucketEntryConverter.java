package jds.s3shell.util;

import com.googlecode.jcsv.writer.CSVEntryConverter;
import jds.s3shell.entities.Bucket;

/**
 * Required by the jcsv library for writing an object to a csv file.
 * @author jsvede
 */
public class BucketEntryConverter implements CSVEntryConverter<Bucket> {
    @Override
    public String[] convertEntry(Bucket bucket) {

        String[] columns = new String[6];
        columns[0] = bucket.getAlias();
        columns[1] = bucket.getBucketName();
        columns[2] = bucket.getAccessKey();
        columns[3] = bucket.getSecretKey();
        columns[4] = bucket.getDescription() == null ? "" : bucket.getDescription();
        columns[5] = bucket.getRegion() == null ? "" : bucket.getRegion();

        return columns;
    }
}

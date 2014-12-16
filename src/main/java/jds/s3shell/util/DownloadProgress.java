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
package jds.s3shell.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * A utility class for writing file read progress to the output stream.
 *
 * @author jsvede
 */
public class DownloadProgress {

    public DownloadProgress() {}

    /**
     * Prints file read progress to the passed in PrintStream.
     *
     * @param fileSize - the total file size.
     * @param bufferSize - the size of the buffer being used to read the stream.
     * @param segments - the number of visual segments desired.
     * @param out - - the PrintStream you want to write the progress to.
     * @throws Exception
     */
    public boolean readFile(InputStream source, String destinationFileName, double fileSize, int bufferSize, long segments, PrintStream out) throws Exception {

        File destinationFile = new File(destinationFileName);

        if(!destinationFile.getParentFile().exists()) {
            destinationFile.getParentFile().mkdirs();
        }
        if(!destinationFile.exists()) {
            destinationFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(destinationFile);

        double segmentSize = fileSize/segments;

        double segmentByteCounter = segmentSize;

        long byteCounter = 0;
        long segmentCounter = 0;
        byte[] buffer = new byte[bufferSize];

        int length = 0;
        while ((length = source.read(buffer)) != -1) {
            fos.write(buffer, 0, length);
            byteCounter += length;

            if(byteCounter > segmentByteCounter) {
                fileProgress((long)fileSize, byteCounter, segmentCounter, segments, out);
                segmentByteCounter += segmentSize;
                segmentCounter++;
            }
        }
        fos.flush();
        fos.close();
        source.close();

        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int a=0; a<64-1;a++){
            sb.append("=");
        }
        sb.append("][" + FileUtils.byteCountToDisplaySize((long) fileSize) + "/" + FileUtils.byteCountToDisplaySize((long)fileSize) + "]");
        System.out.println(sb.toString());

        return true;
    }

    private void fileProgress(long fileSize, long fileProgress, long progress, long destination, PrintStream out) throws Exception {

        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int y=0; y<progress; y++) {
            sb.append("=");
        }
        for(int z=1; z<destination-progress;z++) {
            sb.append(" ");
        }
        sb.append("][" + FileUtils.byteCountToDisplaySize(fileProgress) + "/" + FileUtils.byteCountToDisplaySize((long)fileSize) + "]\r");

        System.out.print(sb.toString());

    }

}

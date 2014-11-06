/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inmobi.grid.fs.s4fs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.s3native.NativeS3FileSystem;

public class NativeS4FileSystem extends NativeS3FileSystem {

    public NativeS4FileSystem() {
        //Default call
        super();
    }

    private URI uri;

    /**
     * fs.default.name in conf is the HDFS store which has credential file for
     * s3n in /user/<name> with bucket name.crd
     * the .crd file contains access:secret in a single line.
     */
    @Override
    public void initialize(URI uri, Configuration conf) throws IOException {

        this.uri = uri;

        if (new Path(conf.get("fs.default.name")).toUri().getScheme().equals("s4")) {
            // currently illegal to set fs.default.name to s4;
            // without this, below code causes recursive call.
            return;
        }

        FileSystem fs = FileSystem.get(conf);
        Path nnWorkingDir = fs.getHomeDirectory();

        if (!fs.exists(nnWorkingDir)) {
            throw new IOException("Users home directory does not exist: " + fs.getWorkingDirectory());
        }

        String scheme = uri.getScheme();
        String bucket = uri.getAuthority();

        Path credFile = new Path(nnWorkingDir, bucket + ".crd");
        if (!fs.exists(credFile)) {
            throw new IOException(credFile.toString() + " does not exists");
        }

        StringBuilder sb = new StringBuilder(getCredentialFromFile(fs, credFile)).append("@").append(bucket);
        String bucketWithAccess = uri.toString().replaceFirst(scheme, "s3n");
        bucketWithAccess = bucketWithAccess.replaceFirst(bucket, sb.toString());

        super.initialize(new Path(bucketWithAccess).toUri(), conf);

    }

    private String getCredentialFromFile(FileSystem fs, Path credFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(credFile)));
        String line = br.readLine();
        if (line == null) {
            throw new IOException("Access:Secret not found in file: " + credFile);
        }
        return line;
    }

    // Need to override this otherwise s3n uri is used with access/secret is
    // returned to clients
    @Override
    public URI getUri() {
        return URI.create(uri.getScheme() + "://" + uri.getAuthority());
    }

}

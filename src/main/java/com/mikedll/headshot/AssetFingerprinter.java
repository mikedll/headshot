package com.mikedll.headshot;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

import java.security.MessageDigest;

public class AssetFingerprinter {

    private Map<String,String> assetToHash = new HashMap<>();

    private String outputDir = "web_assets";

    private String inputDir = "javascript/bundled";
    
    private Instant lastRefreshAt;

    public void setOutputDir(String path) {
        this.outputDir = path;
    }

    public void setInputDir(String path) {
        this.inputDir = path;
    }
    
    public Set<File> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
            .filter(file -> !file.isDirectory())
            .collect(Collectors.toSet());
    }

    /*
     * Update md5 hashes. Copy files with hashes in filenames to web_assets.
     */ 
    public void refresh() {
        synchronized(this) {
            Set<File> files = listFiles(this.inputDir);

            files.forEach(file -> {
                    Instant lastModified = Instant.ofEpochMilli(file.lastModified());
                    if(lastRefreshAt == null || lastModified.compareTo(lastRefreshAt) > 0) {
                        byte[] content = FileUtils.readFileToByteArray(file);
                        MessageDigest md5 = MessageDigest.getInstance("SHA-256");
                        String hash = Hex.encodeHexString(md5.digest(content));
                        assetToHash.put(file.getName(), hash);
                    }
                });
            this.lastRefreshAt = Instant.now();
        }
    }

    public String get(String assetFileName) {
        synchronized(this) {
            return assetToHash.get(assetFileName);
        }
    }
}

package com.mikedll.headshot;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

import java.security.MessageDigest;

public class AssetFingerprinter {

    private static final String ALGORITHM = "SHA-256";
    
    private Map<String,String> assetToFingerprint = new HashMap<>();

    private String outputDir = "web_assets";

    private String inputDir = "javascript/bundled";
    
    private Instant lastRefreshAt;

    public void setOutputDir(String path) {
        this.outputDir = path;
    }

    public void setInputDir(String path) {
        this.inputDir = path;
    }

    /*
     * Only returns files that have a non-empty extension.
     * Ignores .map files.
     */
    public Set<File> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
            .filter(f -> !f.isDirectory())
            .filter(f -> {
                    String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                    return !ext.equals("") && !ext.equals("map");
                })
            .collect(Collectors.toSet());
    }

    /*
     * Update hashes. Copy files with fingerprints in filenames to output dir.
     * Remove any old fingerprinted files in output dir.
     */ 
    public void refresh() {
        synchronized(this) {
            Set<File> inputFiles = listFiles(this.inputDir);
            Set<File> existingOutputFiles = listFiles(this.outputDir);
            
            inputFiles.forEach(inputFile -> {
                    Instant lastModified = Instant.ofEpochMilli(inputFile.lastModified());
                    if(lastRefreshAt == null || lastModified.compareTo(lastRefreshAt) > 0) {
                        byte[] content = null;
                        try {
                            content = FileUtils.readFileToByteArray(inputFile);
                        } catch (IOException ex) {
                            throw new RuntimeException("Error while reading asset file: " + inputFile);
                        }

                        // Calculate fingerprint string
                        MessageDigest messageDigest = null;
                        try {
                            messageDigest = MessageDigest.getInstance(ALGORITHM);
                        } catch (NoSuchAlgorithmException ex) {
                            throw new RuntimeException("NoSuchAlgorithmException for algorithm " + ALGORITHM);
                        }
                        String fingerprint = Hex.encodeHexString(messageDigest.digest(content));
                        assetToFingerprint.put(inputFile.getName(), fingerprint);

                        String inputFileName = inputFile.getName();
                        int dot = inputFileName.lastIndexOf(".");
                        String inputFilePrefix = inputFileName.substring(0, dot);
                        String inputFileExtension = inputFileName.substring(inputFileName.lastIndexOf(".") + 1);
                        String targetOutputName = inputFilePrefix + "-" + fingerprint + "." + inputFileExtension;

                        // Delete old output files that came from this input file
                        Pattern pattern = Pattern.compile(inputFilePrefix + "-\\w{64}\\." + inputFileExtension);
                        existingOutputFiles.stream().filter(outputFile -> {
                                Matcher matcher = pattern.matcher(outputFile.getName());
                                return matcher.find();
                            }).forEach(conflictingOutputFile -> {
                                    try {
                                        FileUtils.delete(conflictingOutputFile);
                                    } catch (IOException ex) {
                                        throw new RuntimeException("IOException when deleting output file " + conflictingOutputFile.getName() + ": " + ex.getMessage());
                                    }
                                });

                        try {
                            FileUtils.copyFile(inputFile, new File(this.outputDir + "/" + targetOutputName));
                        } catch(IOException ex) {
                            throw new RuntimeException("IOException when copying " + targetOutputName + " to output dir " + this.outputDir);
                        }
                    }
                });
                    
            this.lastRefreshAt = Instant.now();
        }
    }

    public String get(String assetFileName) {
        synchronized(this) {
            return getWithoutLock(assetFileName);
        }
    }

    /*
     * Callable in prod as long as you're not recalculating fingerprints.
     * Just calculate them once at startup.
     */
    public String getWithoutLock(String assetFileName) {
        String fingerprint = assetToFingerprint.get(assetFileName);
        if(fingerprint == null) {
            return null;
        }
        
        int dot = assetFileName.lastIndexOf(".");
        String assetFilePrefix = assetFileName.substring(0, dot);
        String assetFileExtension = assetFileName.substring(assetFileName.lastIndexOf(".") + 1);
        String targetBasename = assetFilePrefix + "-" + fingerprint + "." + assetFileExtension;
        return targetBasename;
    }
    
}

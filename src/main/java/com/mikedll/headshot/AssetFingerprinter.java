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
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

import java.security.MessageDigest;

public class AssetFingerprinter {

    private static final String ALGORITHM = "SHA-256";
    
    private Map<String,String> assetToFingerprint = new HashMap<>();

    private String outputPath = "web_assets";

    private String inputPath = "javascript/bundled";
    
    private Instant lastRefreshAt;

    public void setOutputPath(String path) {
        this.outputPath = path;
    }

    public void setInputPath(String path) {
        this.inputPath = path;
    }

    /*
     * Only returns files that have a non-empty extension.
     * Ignores .map files.
     */
    public Set<File> listFiles(String dir) {
        return Optional.ofNullable(new File(dir).listFiles()).map(Arrays::asList).orElse(Collections.emptyList())
            .stream()
            .filter(f -> !f.isDirectory())
            .filter(f -> {
                    String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                    return !ext.equals("") && !ext.equals("map") && !ext.equals("keep");
                })
            .collect(Collectors.toSet());
    }

    /*
     * Update hashes. Copy files with fingerprints in filenames to output dir.
     * Remove any old fingerprinted files in output dir.
     */ 
    public void refresh() {
        synchronized(this) {

            File outputDir = new File(outputPath);
            if(!outputDir.exists()) {
                outputDir.mkdirs();
            }
                
            Set<File> inputFiles = listFiles(this.inputPath);
            Set<File> existingOutputFiles = listFiles(this.outputPath);
            
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
                            FileUtils.copyFile(inputFile, new File(this.outputPath + "/" + targetOutputName));
                        } catch(IOException ex) {
                            throw new RuntimeException("IOException when copying " + targetOutputName + " to output dir " + this.outputPath);
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
     * Shouldn't be used in practice by the app. Just here for testing ease.
     * 
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

    public Map<String,String> getForViews() {
        synchronized(this) {
            return getForViewsWithoutLock();
        }
    }

    /*
     * Callable in prod after refreshing once at startup and no more.
     */
    public Map<String,String> getForViewsWithoutLock() {
        Map<String,String> toReturn = new HashMap<>();

        assetToFingerprint.keySet().forEach(k -> {
                toReturn.put(k, EmbeddedTomcat.PUBLIC_ROOT_DIR + "/" + getWithoutLock(k));
            });

        return toReturn;
    }
}

package com.mikedll.headshot;

import java.io.IOException;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class AssetFingerprinterTests {

    @Test
    public void testRefresh() {
        AssetFingerprinter subject = new AssetFingerprinter();
        subject.setInputDir("src/test/files/assets_fingerprinting");
        subject.setOutputDir("tmp/tool_output");
        subject.refresh();

        Assertions.assertEquals("testSource-3972902cb1170fdb2e6a11c5ff1b8b71f8854cc3aa2ddd0139e805fcf1cdb284.js", subject.get("testSource.js"));
        Assertions.assertEquals("testSource-3972902cb1170fdb2e6a11c5ff1b8b71f8854cc3aa2ddd0139e805fcf1cdb284.js", subject.getWithoutLock("testSource.js"));        
    }

    @Test
    public void testFSUpdates() throws IOException {
        AssetFingerprinter subject = new AssetFingerprinter();
        subject.setInputDir("src/test/files/assets_fingerprinting");
        subject.setOutputDir("tmp/tool_output");

        File toReplace = new File("tmp/tool_output/testSource-e6cc5f3cc2184e0585c5f9230252487153b79b6ee242ae44df1c08eb8c889c51.js");
        FileUtils.write(toReplace, "nothing to see", "UTF-8");

        File toReplace2 = new File("tmp/tool_output/testSource-7343eea1f0c74f7627f1370c148983fa1e499bca4e1641e53aa7797c9436b892.js");
        FileUtils.write(toReplace, "nothing to see", "UTF-8");

        subject.refresh();
        
        Assertions.assertFalse(toReplace.exists());
        Assertions.assertFalse(toReplace2.exists());
        Assertions.assertTrue(new File("tmp/tool_output/testSource-3972902cb1170fdb2e6a11c5ff1b8b71f8854cc3aa2ddd0139e805fcf1cdb284.js").exists());
    }    
}

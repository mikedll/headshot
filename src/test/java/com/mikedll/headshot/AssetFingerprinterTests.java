package com.mikedll.headshot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class AssetFingerprinterTests {

    @Test
    public void testRefresh() {
        AssetFingerprinter subject = new AssetFingerprinter();
        subject.setInputDir("src/test/files/assets_fingerprinting");
        subject.setOutputDir("tmp/tool_output");
        subject.refresh();

        Assertions.assertEquals("34343fdfdfd", subject.get("testSource.js"));
    }
}

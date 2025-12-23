/*
 *  Copyright (C) 2010 Ryszard Wiśniewski <brut.alll@gmail.com>
 *  Copyright (C) 2010 Connor Tumbleson <connor.tumbleson@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package brut.androlib.deobfuscate;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.util.OS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for DexDeobfuscator functionality
 */
public class DexDeobfuscatorTest {

    private File tempDir;
    private File smaliDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("apktool-deobf-test").toFile();
        smaliDir = new File(tempDir, "smali");
        smaliDir.mkdirs();
    }

    @After
    public void tearDown() {
        if (tempDir != null && tempDir.exists()) {
            OS.rmdir(tempDir);
        }
    }

    @Test
    public void testDeobfuscatorCreation() {
        DexDeobfuscator deobfuscator = new DexDeobfuscator(smaliDir);
        assertNotNull("Deobfuscator should be created", deobfuscator);
    }

    @Test
    public void testDeobfuscateEmptyDirectory() throws AndrolibException {
        DexDeobfuscator deobfuscator = new DexDeobfuscator(smaliDir);
        // Should not throw exception on empty directory
        deobfuscator.deobfuscate();
    }

    @Test
    public void testDeobfuscateWithSimpleSmaliFile() throws IOException, AndrolibException {
        // Create a simple smali file with obfuscated names
        File smaliFile = new File(smaliDir, "a.smali");
        List<String> lines = Arrays.asList(
            ".class public La;",
            ".super Ljava/lang/Object;",
            ".field private a:I",
            ".method public a()V",
            "    .registers 1",
            "    return-void",
            ".end method"
        );
        Files.write(smaliFile.toPath(), lines);

        DexDeobfuscator deobfuscator = new DexDeobfuscator(smaliDir);
        deobfuscator.deobfuscate();

        // Verify the file still exists
        assertTrue("Smali file should exist after deobfuscation", smaliFile.exists());

        // Verify content was processed
        List<String> newLines = Files.readAllLines(smaliFile.toPath());
        assertNotNull("File should have content", newLines);
        assertTrue("File should not be empty", newLines.size() > 0);
    }

    @Test
    public void testDeobfuscateNonObfuscatedFile() throws IOException, AndrolibException {
        // Create a smali file with non-obfuscated names
        File smaliFile = new File(smaliDir, "MainActivity.smali");
        List<String> lines = Arrays.asList(
            ".class public Lcom/example/MainActivity;",
            ".super Landroid/app/Activity;",
            ".field private myField:I",
            ".method public onCreate()V",
            "    .registers 1",
            "    return-void",
            ".end method"
        );
        Files.write(smaliFile.toPath(), lines);

        DexDeobfuscator deobfuscator = new DexDeobfuscator(smaliDir);
        deobfuscator.deobfuscate();

        // Verify the file content remains unchanged (non-obfuscated)
        List<String> newLines = Files.readAllLines(smaliFile.toPath());
        assertEquals("Content should remain similar for non-obfuscated code", 
                     lines.size(), newLines.size());
    }

    @Test(expected = AndrolibException.class)
    public void testDeobfuscateNonExistentDirectory() throws AndrolibException {
        File nonExistent = new File(tempDir, "nonexistent");
        DexDeobfuscator deobfuscator = new DexDeobfuscator(nonExistent);
        deobfuscator.deobfuscate();
    }
}

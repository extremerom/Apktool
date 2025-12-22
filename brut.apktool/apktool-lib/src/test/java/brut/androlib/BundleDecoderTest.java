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
package brut.androlib;

import org.junit.Test;
import java.io.File;

import static org.junit.Assert.*;

/**
 * Tests for BundleDecoder functionality
 */
public class BundleDecoderTest {

    @Test
    public void testIsBundleFile_APKM() {
        File file = new File("test.apkm");
        assertTrue("Should detect .apkm as bundle format", BundleDecoder.isBundleFile(file));
    }

    @Test
    public void testIsBundleFile_APKS() {
        File file = new File("test.apks");
        assertTrue("Should detect .apks as bundle format", BundleDecoder.isBundleFile(file));
    }

    @Test
    public void testIsBundleFile_XAPK() {
        File file = new File("test.xapk");
        assertTrue("Should detect .xapk as bundle format", BundleDecoder.isBundleFile(file));
    }

    @Test
    public void testIsBundleFile_APK() {
        File file = new File("test.apk");
        assertFalse("Should not detect .apk as bundle format", BundleDecoder.isBundleFile(file));
    }

    @Test
    public void testIsBundleFile_CaseInsensitive() {
        File file1 = new File("test.APKM");
        File file2 = new File("test.ApKs");
        File file3 = new File("test.XaPk");
        
        assertTrue("Should detect .APKM (uppercase) as bundle format", BundleDecoder.isBundleFile(file1));
        assertTrue("Should detect .ApKs (mixed case) as bundle format", BundleDecoder.isBundleFile(file2));
        assertTrue("Should detect .XaPk (mixed case) as bundle format", BundleDecoder.isBundleFile(file3));
    }

    @Test
    public void testIsBundleFile_OtherFormats() {
        File file1 = new File("test.zip");
        File file2 = new File("test.jar");
        File file3 = new File("test.txt");
        
        assertFalse("Should not detect .zip as bundle format", BundleDecoder.isBundleFile(file1));
        assertFalse("Should not detect .jar as bundle format", BundleDecoder.isBundleFile(file2));
        assertFalse("Should not detect .txt as bundle format", BundleDecoder.isBundleFile(file3));
    }
}

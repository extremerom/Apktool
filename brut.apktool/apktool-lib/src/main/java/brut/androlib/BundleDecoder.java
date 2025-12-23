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

import brut.androlib.exceptions.AndrolibException;
import brut.directory.ExtFile;
import brut.util.OS;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Handles decoding of bundle formats like APKM, APKS, XAPK
 * These formats are essentially ZIP files containing multiple APKs
 */
public class BundleDecoder {
    private static final Logger LOGGER = Logger.getLogger(BundleDecoder.class.getName());
    private static final Pattern BASE_APK_PATTERN = Pattern.compile(".*[^a-z]base[^a-z].*\\.apk", Pattern.CASE_INSENSITIVE);
    
    private final File mBundleFile;
    private final Config mConfig;
    
    public BundleDecoder(File bundleFile, Config config) {
        mBundleFile = bundleFile;
        mConfig = config;
    }
    
    /**
     * Checks if the file is a bundle format (APKM, APKS, XAPK)
     */
    public static boolean isBundleFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".apkm") || name.endsWith(".apks") || name.endsWith(".xapk");
    }
    
    /**
     * Extracts and decodes all APKs from the bundle
     */
    public void decode(File outDir) throws AndrolibException {
        LOGGER.info("Detected bundle format: " + mBundleFile.getName());
        
        File extractDir = new File(outDir, "bundle_extracted");
        try {
            OS.rmdir(extractDir);
            OS.mkdir(extractDir);
            
            List<File> apkFiles = extractApksFromBundle(extractDir);
            
            if (apkFiles.isEmpty()) {
                throw new AndrolibException("No APK files found in bundle: " + mBundleFile.getName());
            }
            
            LOGGER.info("Found " + apkFiles.size() + " APK(s) in bundle");
            
            // Find the base/main APK
            File mainApk = findMainApk(apkFiles);
            if (mainApk == null) {
                LOGGER.warning("Could not identify main APK, using first APK");
                mainApk = apkFiles.get(0);
            }
            
            LOGGER.info("Decoding main APK: " + mainApk.getName());
            
            // Decode the main APK
            try (ExtFile apkFile = new ExtFile(mainApk)) {
                ApkDecoder decoder = new ApkDecoder(apkFile, mConfig);
                decoder.decode(outDir);
            }
            
            // Copy other APKs to splits directory for reference
            if (apkFiles.size() > 1) {
                File splitsDir = new File(outDir, "splits");
                OS.mkdir(splitsDir);
                
                for (File apk : apkFiles) {
                    if (!apk.equals(mainApk)) {
                        File dest = new File(splitsDir, apk.getName());
                        copyFile(apk, dest);
                        LOGGER.info("Split APK saved: " + apk.getName());
                    }
                }
            }
            
        } catch (IOException ex) {
            throw new AndrolibException("Error processing bundle: " + ex.getMessage(), ex);
        } finally {
            // Clean up extracted files if needed
            if (!mConfig.isVerbose()) {
                OS.rmdir(extractDir);
            }
        }
    }
    
    /**
     * Extracts APK files from the bundle
     */
    private List<File> extractApksFromBundle(File extractDir) throws IOException, AndrolibException {
        List<File> apkFiles = new ArrayList<>();
        
        try (ZipFile zipFile = new ZipFile(mBundleFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                // Extract APK files
                if (entryName.toLowerCase().endsWith(".apk") && !entry.isDirectory()) {
                    File outputFile = new File(extractDir, new File(entryName).getName());
                    
                    try (InputStream is = zipFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(outputFile)) {
                        IOUtils.copy(is, fos);
                    }
                    
                    apkFiles.add(outputFile);
                    LOGGER.fine("Extracted: " + entryName);
                }
            }
        }
        
        return apkFiles;
    }
    
    /**
     * Identifies the main/base APK from a list of APK files
     * The main APK is typically named "base.apk" or has no split configuration
     */
    private File findMainApk(List<File> apkFiles) {
        // Look for base.apk or similar names
        for (File apk : apkFiles) {
            String name = apk.getName().toLowerCase();
            if (name.equals("base.apk") || name.contains("base.") || 
                BASE_APK_PATTERN.matcher(name).matches()) {
                return apk;
            }
        }
        
        // Look for files without split identifiers
        for (File apk : apkFiles) {
            String name = apk.getName().toLowerCase();
            if (!name.contains("split") && !name.contains("config.")) {
                return apk;
            }
        }
        
        return null;
    }
    
    /**
     * Copies a file
     */
    private void copyFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(dest)) {
            IOUtils.copy(is, os);
        }
    }
}

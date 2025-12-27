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
package brut.androlib.dex;

import brut.androlib.exceptions.AndrolibException;
import brut.directory.Directory;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import brut.util.OS;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Runner for additional DEX analysis tools (dex2jar, jadx, smali/baksmali, Androguard)
 */
public class DexAnalysisToolsRunner {
    private static final Logger LOGGER = Logger.getLogger(DexAnalysisToolsRunner.class.getName());

    private final ExtFile mApkFile;
    private final File mOutputDir;

    public DexAnalysisToolsRunner(ExtFile apkFile, File outputDir) {
        mApkFile = apkFile;
        mOutputDir = outputDir;
    }

    /**
     * Run all available analysis tools on DEX files
     */
    public void runAnalysisTools() throws AndrolibException {
        File toolsDir = new File(mOutputDir, "tools");
        OS.mkdir(toolsDir);

        LOGGER.info("Running additional DEX analysis tools...");
        
        // Generate README documentation
        ToolsReadmeGenerator.generateReadme(toolsDir);

        try {
            Directory in = mApkFile.getDirectory();
            List<String> dexFiles = findDexFiles(in);

            if (dexFiles.isEmpty()) {
                LOGGER.warning("No DEX files found in APK");
                return;
            }

            // Extract DEX files to tools directory for processing
            for (String dexFile : dexFiles) {
                extractDexFile(in, dexFile, toolsDir);
            }

            // Run analysis tools
            runDex2Jar(toolsDir, dexFiles);
            runJadx(toolsDir, dexFiles);
            runSmaliAnalysis(toolsDir, dexFiles);
            runAndroguard(toolsDir, dexFiles);

            LOGGER.info("Analysis tools completed. Results saved in: " + toolsDir.getPath());
        } catch (DirectoryException | IOException ex) {
            throw new AndrolibException("Failed to run analysis tools: " + ex.getMessage(), ex);
        }
    }

    private List<String> findDexFiles(Directory directory) throws DirectoryException {
        List<String> dexFiles = new ArrayList<>();
        for (String fileName : directory.getFiles(true)) {
            if (fileName.endsWith(".dex")) {
                dexFiles.add(fileName);
            }
        }
        return dexFiles;
    }

    private void extractDexFile(Directory in, String dexFileName, File toolsDir) throws DirectoryException, IOException {
        File dexFile = new File(toolsDir, dexFileName);
        try (InputStream is = in.getFileInput(dexFileName)) {
            Files.copy(is, dexFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Validates a file path to prevent command injection
     * @param file File to validate
     * @throws AndrolibException if the path is potentially unsafe
     */
    private void validateFilePath(File file) throws AndrolibException {
        try {
            String canonicalPath = file.getCanonicalPath();
            
            // Ensure the file is within expected directory
            if (!canonicalPath.startsWith(mOutputDir.getCanonicalPath())) {
                throw new AndrolibException("File path outside of output directory: " + canonicalPath);
            }
            
            // Check for null bytes and other suspicious characters
            if (canonicalPath.contains("\0") || canonicalPath.contains(";") || 
                canonicalPath.contains("&") || canonicalPath.contains("|") ||
                canonicalPath.contains("`") || canonicalPath.contains("$")) {
                throw new AndrolibException("File path contains potentially dangerous characters: " + canonicalPath);
            }
        } catch (IOException e) {
            throw new AndrolibException("Failed to validate file path: " + file.getPath(), e);
        }
    }

    private void runDex2Jar(File toolsDir, List<String> dexFiles) {
        File dex2jarDir = new File(toolsDir, "dex2jar");
        OS.mkdir(dex2jarDir);

        LOGGER.info("Running dex2jar conversion...");
        try {
            // Validate directories before using them
            validateFilePath(toolsDir);
            validateFilePath(dex2jarDir);
            
            for (String dexFileName : dexFiles) {
                File dexFile = new File(toolsDir, dexFileName);
                String jarFileName = dexFileName.replace(".dex", ".jar");
                File jarFile = new File(dex2jarDir, jarFileName);
                
                // Validate file paths
                validateFilePath(dexFile);
                validateFilePath(jarFile);

                // Try to use dex2jar if available in PATH
                ProcessBuilder pb = new ProcessBuilder("d2j-dex2jar", 
                    dexFile.getAbsolutePath(), 
                    "-o", jarFile.getAbsolutePath());
                pb.redirectErrorStream(true);

                try {
                    Process process = pb.start();
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        LOGGER.info("dex2jar: Converted " + dexFileName + " to " + jarFileName);
                    } else {
                        LOGGER.warning("dex2jar: Failed to convert " + dexFileName);
                    }
                } catch (IOException | InterruptedException e) {
                    LOGGER.warning("dex2jar not available in PATH. Skipping dex2jar conversion.");
                    writeToolNotAvailableInfo(dex2jarDir, "dex2jar", 
                        "Install dex2jar from: https://github.com/pxb1988/dex2jar");
                    break;
                }
            }
        } catch (Exception ex) {
            LOGGER.warning("dex2jar analysis failed: " + ex.getMessage());
        }
    }

    private void runJadx(File toolsDir, List<String> dexFiles) {
        File jadxDir = new File(toolsDir, "jadx");
        OS.mkdir(jadxDir);

        LOGGER.info("Running JADX decompilation...");
        try {
            // Validate directories
            validateFilePath(toolsDir);
            validateFilePath(jadxDir);
            
            // Try to use jadx if available in PATH
            File apkPath = mApkFile.getAbsoluteFile();
            validateFilePath(apkPath);
            
            ProcessBuilder pb = new ProcessBuilder("jadx",
                "-d", jadxDir.getAbsolutePath(),
                "--no-res",
                apkPath.getAbsolutePath());
            pb.redirectErrorStream(true);

            try {
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    LOGGER.info("JADX: Decompilation completed");
                } else {
                    LOGGER.warning("JADX: Decompilation failed");
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.warning("JADX not available in PATH. Skipping JADX decompilation.");
                writeToolNotAvailableInfo(jadxDir, "jadx",
                    "Install JADX from: https://github.com/skylot/jadx");
            }
        } catch (Exception ex) {
            LOGGER.warning("JADX analysis failed: " + ex.getMessage());
        }
    }

    private void runSmaliAnalysis(File toolsDir, List<String> dexFiles) {
        File smaliDir = new File(toolsDir, "smali-analysis");
        OS.mkdir(smaliDir);

        LOGGER.info("Running smali/baksmali analysis...");
        try {
            // Create a summary file with smali info
            File summaryFile = new File(smaliDir, "smali-info.txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(summaryFile))) {
                writer.println("Smali/Baksmali Analysis Summary");
                writer.println("================================");
                writer.println();
                writer.println("DEX files found: " + dexFiles.size());
                for (String dexFile : dexFiles) {
                    writer.println("  - " + dexFile);
                }
                writer.println();
                writer.println("Note: Full smali disassembly is available in the main 'smali' directories");
                writer.println("created by Apktool's standard decode process.");
                writer.println();
                writer.println("Smali is already integrated into Apktool.");
                writer.println("For more information, visit: https://github.com/google/smali");
            }
            LOGGER.info("Smali/Baksmali: Analysis info written");
        } catch (Exception ex) {
            LOGGER.warning("Smali/Baksmali analysis failed: " + ex.getMessage());
        }
    }

    private void runAndroguard(File toolsDir, List<String> dexFiles) {
        File androguardDir = new File(toolsDir, "androguard");
        OS.mkdir(androguardDir);

        LOGGER.info("Running Androguard analysis...");
        try {
            // Validate directories
            validateFilePath(toolsDir);
            validateFilePath(androguardDir);
            
            // Try to use androguard if available in PATH
            File apkPath = mApkFile.getAbsoluteFile();
            validateFilePath(apkPath);
            
            File outputFile = new File(androguardDir, "androguard-analysis.txt");
            validateFilePath(outputFile);
            
            // Try androlyze for basic analysis
            ProcessBuilder pb = new ProcessBuilder("androguard", "axml",
                apkPath.getAbsolutePath());
            pb.redirectErrorStream(true);
            pb.redirectOutput(outputFile);

            try {
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    LOGGER.info("Androguard: Analysis completed");
                } else {
                    LOGGER.warning("Androguard: Analysis failed");
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.warning("Androguard not available in PATH. Skipping Androguard analysis.");
                writeToolNotAvailableInfo(androguardDir, "Androguard",
                    "Install Androguard from: https://github.com/androguard/androguard\n" +
                    "Using: pip install androguard");
            }
        } catch (Exception ex) {
            LOGGER.warning("Androguard analysis failed: " + ex.getMessage());
        }
    }

    private void writeToolNotAvailableInfo(File dir, String toolName, String installInfo) {
        try {
            File infoFile = new File(dir, "README.txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(infoFile))) {
                writer.println(toolName + " - Not Available");
                writer.println("=".repeat(toolName.length() + 18));
                writer.println();
                writer.println("The " + toolName + " tool was not found in your system PATH.");
                writer.println();
                writer.println(installInfo);
                writer.println();
                writer.println("After installation, make sure the tool is available in your system PATH");
                writer.println("and run Apktool decode again with the --use-analysis-tools option.");
            }
        } catch (IOException ex) {
            LOGGER.warning("Failed to write info file for " + toolName);
        }
    }
}

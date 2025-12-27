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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Generator for README documentation in the tools directory
 */
public class ToolsReadmeGenerator {
    private static final Logger LOGGER = Logger.getLogger(ToolsReadmeGenerator.class.getName());

    public static void generateReadme(File toolsDir) {
        File readmeFile = new File(toolsDir, "README.md");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(readmeFile))) {
            writer.println("# DEX Analysis Tools Directory");
            writer.println();
            writer.println("This directory contains the output from additional DEX analysis tools that were run during APK decompilation.");
            writer.println();
            writer.println("## Overview");
            writer.println();
            writer.println("When you decode an APK with the `--use-analysis-tools` option, Apktool runs several popular");
            writer.println("Android reverse engineering tools to provide comprehensive analysis of the DEX files:");
            writer.println();
            writer.println("### Available Tools");
            writer.println();
            writer.println("#### 1. dex2jar");
            writer.println("**Directory:** `dex2jar/`");
            writer.println();
            writer.println("Converts DEX files to JAR files, which can then be opened with Java decompilers like JD-GUI or Fernflower.");
            writer.println();
            writer.println("- **Website:** https://github.com/pxb1988/dex2jar");
            writer.println("- **Output:** `.jar` files for each `.dex` file found");
            writer.println("- **Usage:** Open the generated JAR files with any Java decompiler");
            writer.println();
            writer.println("**Installation:**");
            writer.println("```bash");
            writer.println("# On macOS with Homebrew");
            writer.println("brew install dex2jar");
            writer.println();
            writer.println("# On Linux");
            writer.println("# Download from GitHub releases and add to PATH");
            writer.println("```");
            writer.println();
            writer.println("#### 2. JADX");
            writer.println("**Directory:** `jadx/`");
            writer.println();
            writer.println("Dex to Java decompiler. Produces Java source code from Android Dex and APK files.");
            writer.println();
            writer.println("- **Website:** https://github.com/skylot/jadx");
            writer.println("- **Output:** Decompiled Java source code");
            writer.println("- **Usage:** Browse the `sources/` subdirectory for decompiled Java files");
            writer.println();
            writer.println("**Installation:**");
            writer.println("```bash");
            writer.println("# On macOS with Homebrew");
            writer.println("brew install jadx");
            writer.println();
            writer.println("# On Linux");
            writer.println("# Download from GitHub releases and add to PATH");
            writer.println();
            writer.println("# Or use GUI version");
            writer.println("# Download jadx-gui from releases");
            writer.println("```");
            writer.println();
            writer.println("#### 3. smali/baksmali");
            writer.println("**Directory:** `smali-analysis/`");
            writer.println();
            writer.println("Smali/baksmali is an assembler/disassembler for the dex format used by dalvik.");
            writer.println();
            writer.println("- **Website:** https://github.com/google/smali");
            writer.println("- **Note:** Already integrated into Apktool's main decode process");
            writer.println("- **Output:** Reference information (actual smali files are in the main `smali/` directories)");
            writer.println();
            writer.println("**Note:** You don't need to install smali separately. It's already part of Apktool.");
            writer.println();
            writer.println("#### 4. Androguard");
            writer.println("**Directory:** `androguard/`");
            writer.println();
            writer.println("A full Python tool to analyze Android applications with advanced features like:");
            writer.println("- Control flow graphs");
            writer.println("- Call graphs");
            writer.println("- Security analysis");
            writer.println();
            writer.println("- **Website:** https://github.com/androguard/androguard");
            writer.println("- **Output:** Analysis reports and XML structure information");
            writer.println();
            writer.println("**Installation:**");
            writer.println("```bash");
            writer.println("# Using pip");
            writer.println("pip install androguard");
            writer.println();
            writer.println("# Or using pipx (recommended)");
            writer.println("pipx install androguard");
            writer.println("```");
            writer.println();
            writer.println("## Usage");
            writer.println();
            writer.println("To enable this feature, decode your APK with the `--use-analysis-tools` flag:");
            writer.println();
            writer.println("```bash");
            writer.println("apktool d --use-analysis-tools your-app.apk");
            writer.println("```");
            writer.println();
            writer.println("This will create a `tools/` directory in the output containing results from all available tools.");
            writer.println();
            writer.println("## Tool Availability");
            writer.println();
            writer.println("If a tool is not installed on your system, Apktool will:");
            writer.println("1. Skip that particular tool");
            writer.println("2. Create a `README.txt` in that tool's directory with installation instructions");
            writer.println("3. Continue with other available tools");
            writer.println();
            writer.println("## Extracted DEX Files");
            writer.println();
            writer.println("The original DEX files extracted from the APK are placed directly in the `tools/` directory");
            writer.println("(e.g., `classes.dex`, `classes2.dex`, etc.). These can be used manually with other tools");
            writer.println("if needed.");
            writer.println();
            writer.println("## Comparing Tools");
            writer.println();
            writer.println("Each tool has different strengths:");
            writer.println();
            writer.println("| Tool | Strength | Best For |");
            writer.println("|------|----------|----------|");
            writer.println("| **smali/baksmali** | Low-level disassembly | Understanding exact bytecode, debugging |");
            writer.println("| **dex2jar + JD-GUI** | JAR conversion | Quick viewing with familiar Java tools |");
            writer.println("| **JADX** | Direct Java decompilation | Reading high-level Java code quickly |");
            writer.println("| **Androguard** | Advanced analysis | Security research, flow analysis |");
            writer.println();
            writer.println("## Tips");
            writer.println();
            writer.println("1. **Start with JADX** for a quick overview of the Java code");
            writer.println("2. **Use smali** when you need to see exact bytecode or make precise modifications");
            writer.println("3. **Use dex2jar + decompiler** if you prefer working with JAR files");
            writer.println("4. **Use Androguard** for security analysis or advanced static analysis");
            writer.println();
            writer.println("## Further Reading");
            writer.println();
            writer.println("- [Apktool Documentation](https://apktool.org)");
            writer.println("- [Android Reverse Engineering Guide](https://github.com/b-mueller/owasp-mstg)");
            writer.println("- [Smali Language](https://github.com/JesusFreke/smali/wiki)");
            writer.println();
            writer.println("---");
            writer.println();
            writer.println("*Generated by Apktool " + getApktoolVersion() + "*");
            
            LOGGER.info("Generated README.md in tools directory");
        } catch (IOException ex) {
            LOGGER.warning("Failed to generate README.md: " + ex.getMessage());
        }
    }
    
    private static String getApktoolVersion() {
        // Try to get version, return unknown if not available
        try {
            return brut.androlib.Config.class.getPackage().getImplementationVersion();
        } catch (Exception e) {
            return "3.0.0-SNAPSHOT";
        }
    }
}

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

import brut.androlib.exceptions.AndrolibException;
import brut.util.OS;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DEX Deobfuscator - provides basic deobfuscation for smali files
 * Inspired by JADX deobfuscation features
 */
public class DexDeobfuscator {
    private static final Logger LOGGER = Logger.getLogger(DexDeobfuscator.class.getName());
    
    private final File mSmaliDir;
    private final Map<String, String> mClassNameMap = new HashMap<>();
    private final Map<String, String> mFieldNameMap = new HashMap<>();
    private final Map<String, String> mMethodNameMap = new HashMap<>();
    
    // Patterns for obfuscated names (single letter, or short random strings)
    private static final Pattern OBFUSCATED_CLASS_PATTERN = Pattern.compile("L([a-z]/)+[a-z];");
    private static final Pattern OBFUSCATED_FIELD_PATTERN = Pattern.compile("^\\.field .* ([a-z]):.*$");
    private static final Pattern OBFUSCATED_METHOD_PATTERN = Pattern.compile("^\\.method .* ([a-z])\\(.*$");
    
    private int classCounter = 0;
    private int fieldCounter = 0;
    private int methodCounter = 0;
    
    public DexDeobfuscator(File smaliDir) {
        mSmaliDir = smaliDir;
    }
    
    /**
     * Performs deobfuscation on smali files
     */
    public void deobfuscate() throws AndrolibException {
        if (!mSmaliDir.exists() || !mSmaliDir.isDirectory()) {
            throw new AndrolibException("Smali directory not found: " + mSmaliDir);
        }
        
        LOGGER.info("Starting deobfuscation process...");
        
        try {
            // Phase 1: Analyze and build rename map
            analyzeSmaliFiles(mSmaliDir);
            
            // Phase 2: Apply renames
            if (!mClassNameMap.isEmpty() || !mFieldNameMap.isEmpty() || !mMethodNameMap.isEmpty()) {
                LOGGER.info(String.format("Deobfuscation map: %d classes, %d fields, %d methods",
                        mClassNameMap.size(), mFieldNameMap.size(), mMethodNameMap.size()));
                applyRenames(mSmaliDir);
                LOGGER.info("Deobfuscation completed successfully");
            } else {
                LOGGER.info("No obfuscated names detected");
            }
            
        } catch (IOException ex) {
            throw new AndrolibException("Error during deobfuscation: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Analyzes smali files to identify obfuscated names
     */
    private void analyzeSmaliFiles(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                analyzeSmaliFiles(file);
            } else if (file.getName().endsWith(".smali")) {
                analyzeSmaliFile(file);
            }
        }
    }
    
    /**
     * Analyzes a single smali file
     */
    private void analyzeSmaliFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        
        for (String line : lines) {
            line = line.trim();
            
            // Check for obfuscated class names
            if (line.startsWith(".class")) {
                String className = extractClassName(line);
                if (className != null && isObfuscatedClassName(className)) {
                    if (!mClassNameMap.containsKey(className)) {
                        mClassNameMap.put(className, generateClassName());
                    }
                }
            }
            
            // Check for obfuscated field names
            if (line.startsWith(".field")) {
                String fieldName = extractFieldName(line);
                if (fieldName != null && isObfuscatedName(fieldName)) {
                    String key = file.getName() + ":" + fieldName;
                    if (!mFieldNameMap.containsKey(key)) {
                        mFieldNameMap.put(key, generateFieldName());
                    }
                }
            }
            
            // Check for obfuscated method names (excluding constructors)
            if (line.startsWith(".method") && !line.contains("<init>") && !line.contains("<clinit>")) {
                String methodName = extractMethodName(line);
                if (methodName != null && isObfuscatedName(methodName)) {
                    String key = file.getName() + ":" + methodName;
                    if (!mMethodNameMap.containsKey(key)) {
                        mMethodNameMap.put(key, generateMethodName());
                    }
                }
            }
        }
    }
    
    /**
     * Applies the rename mappings to all smali files
     */
    private void applyRenames(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                applyRenames(file);
            } else if (file.getName().endsWith(".smali")) {
                renameInFile(file);
            }
        }
    }
    
    /**
     * Applies renames within a single smali file
     */
    private void renameInFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        List<String> newLines = new ArrayList<>();
        boolean modified = false;
        
        for (String line : lines) {
            String newLine = line;
            
            // Replace class names
            for (Map.Entry<String, String> entry : mClassNameMap.entrySet()) {
                if (newLine.contains(entry.getKey())) {
                    newLine = newLine.replace(entry.getKey(), entry.getValue());
                    modified = true;
                }
            }
            
            // Replace field and method names for this file
            for (Map.Entry<String, String> entry : mFieldNameMap.entrySet()) {
                if (entry.getKey().startsWith(file.getName() + ":")) {
                    String oldName = entry.getKey().substring(file.getName().length() + 1);
                    if (newLine.contains(" " + oldName + " ") || newLine.contains(" " + oldName + ":")) {
                        newLine = newLine.replaceAll("\\b" + Pattern.quote(oldName) + "\\b", entry.getValue());
                        modified = true;
                    }
                }
            }
            
            for (Map.Entry<String, String> entry : mMethodNameMap.entrySet()) {
                if (entry.getKey().startsWith(file.getName() + ":")) {
                    String oldName = entry.getKey().substring(file.getName().length() + 1);
                    if (newLine.contains(" " + oldName + "(")) {
                        newLine = newLine.replaceAll("\\b" + Pattern.quote(oldName) + "\\(", entry.getValue() + "(");
                        modified = true;
                    }
                }
            }
            
            newLines.add(newLine);
        }
        
        // Write back if modified
        if (modified) {
            Files.write(file.toPath(), newLines);
        }
    }
    
    // Helper methods for extracting names from smali lines
    
    private String extractClassName(String line) {
        // Extract class name from ".class ... Lpath/to/ClassName;"
        int lastSpace = line.lastIndexOf(' ');
        if (lastSpace > 0) {
            return line.substring(lastSpace + 1).trim();
        }
        return null;
    }
    
    private String extractFieldName(String line) {
        // Extract field name from ".field ... name:Type"
        String[] parts = line.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].contains(":")) {
                return parts[i].substring(0, parts[i].indexOf(':'));
            }
        }
        return null;
    }
    
    private String extractMethodName(String line) {
        // Extract method name from ".method ... name(...)..."
        String[] parts = line.split("\\s+");
        for (String part : parts) {
            if (part.contains("(")) {
                return part.substring(0, part.indexOf('('));
            }
        }
        return null;
    }
    
    // Obfuscation detection
    
    private boolean isObfuscatedClassName(String className) {
        // Simple heuristic: class name is a single letter or very short
        if (!className.startsWith("L") || !className.endsWith(";")) {
            return false;
        }
        
        String name = className.substring(className.lastIndexOf('/') + 1, className.length() - 1);
        return name.length() <= 2 && name.matches("[a-z]+");
    }
    
    private boolean isObfuscatedName(String name) {
        // Simple heuristic: name is a single letter
        return name.length() <= 2 && name.matches("[a-z]+");
    }
    
    // Name generators
    
    private String generateClassName() {
        return "DeobfClass" + (++classCounter);
    }
    
    private String generateFieldName() {
        return "field" + (++fieldCounter);
    }
    
    private String generateMethodName() {
        return "method" + (++methodCounter);
    }
}

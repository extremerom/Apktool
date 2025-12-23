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
package brut.androlib.smali;

import brut.androlib.exceptions.AndrolibException;
import brut.util.OS;
import com.android.tools.smali.baksmali.Baksmali;
import com.android.tools.smali.baksmali.BaksmaliOptions;
import com.android.tools.smali.dexlib2.DexFileFactory;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.analysis.InlineMethodResolver;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedOdexFile;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.MultiDexContainer;

import java.io.*;
import java.util.ArrayList;

public class SmaliDecoder {
    private final File mApkFile;
    private final String mDexName;
    private final boolean mBakDeb;
    private int mInferredApiLevel;

    public SmaliDecoder(File apkFile, String dexName, boolean bakDeb) {
        mApkFile = apkFile;
        mDexName = dexName;
        mBakDeb = bakDeb;
    }

    public int getInferredApiLevel() {
        return mInferredApiLevel;
    }

    public void decode(File outDir) throws AndrolibException {
        try {
            // #3641 - Limit opcode API level to match SmaliBuilder to avoid verification errors.
            // Load the container first to determine the original API level, then reload with
            // limited opcodes to ensure consistent disassembly/assembly.
            Opcodes limitedOpcodes;
            try {
                // First load with default opcodes to determine the original API level
                MultiDexContainer<? extends DexBackedDexFile> tempContainer =
                    DexFileFactory.loadDexContainer(mApkFile, null);
                
                // Check if we have any dex entries
                if (tempContainer.getDexEntryNames().isEmpty()) {
                    throw new AndrolibException("No dex entries found in: " + mApkFile.getName());
                }
                
                String firstEntry = tempContainer.getDexEntryNames().get(0);
                DexBackedDexFile tempDexFile = tempContainer.getEntry(firstEntry).getDexFile();
                int originalApiLevel = tempDexFile.getOpcodes().api;
                
                // Limit to MAX_SUPPORTED_API_LEVEL if needed
                int limitedApiLevel = Math.min(originalApiLevel, SmaliConstants.MAX_SUPPORTED_API_LEVEL);
                limitedOpcodes = Opcodes.forApi(limitedApiLevel);
            } catch (IOException e) {
                // If we can't determine the API level, use default opcodes limited to MAX_SUPPORTED_API_LEVEL
                limitedOpcodes = Opcodes.forApi(SmaliConstants.MAX_SUPPORTED_API_LEVEL);
            }
            
            // Create the container with limited opcodes.
            MultiDexContainer<? extends DexBackedDexFile> container =
                DexFileFactory.loadDexContainer(mApkFile, limitedOpcodes);
            ArrayList<MultiDexContainer.DexEntry<? extends DexBackedDexFile>> dexEntries = new ArrayList<>();
            DexBackedDexFile dexFile = null;
            boolean isDexContainerFormat = false;

            if (isDexContainerFormat = isDexContainerFormat(container)) {
                for (String entry : container.getDexEntryNames()) {
                    dexEntries.add(container.getEntry(entry));
                }
             } else {
                dexEntries.add(container.getEntry(mDexName));
             }

            // Double-check the passed param exists.
            if (dexEntries.isEmpty()) {
                dexEntries.add(container.getEntry(container.getDexEntryNames().get(0)));
            }

            assert !dexEntries.isEmpty();

            for (MultiDexContainer.DexEntry<? extends DexBackedDexFile> dexEntry : dexEntries) {
                File smaliDir = outDir;
                if (isDexContainerFormat) {
                    int index = dexEntries.indexOf(dexEntry) + 1;
                    if (index > 1) {
                        smaliDir = new File(outDir.getParent(), "smali_classes" + index);
                        OS.rmdir(smaliDir);
                        OS.mkdir(smaliDir);
                    }
                }
                dexFile = decodeInternal(dexEntry, smaliDir);
            }

            // The API level is already limited during loading, but we store it for reference
            mInferredApiLevel = dexFile.getOpcodes().api;
        } catch (IOException ex) {
            throw new AndrolibException("Could not baksmali file: " + mDexName, ex);
        }
    }

    private DexBackedDexFile decodeInternal(MultiDexContainer.DexEntry<? extends DexBackedDexFile> dexEntry,
            File outDir) throws IOException, AndrolibException {
        BaksmaliOptions options = new BaksmaliOptions();
        options.deodex = false;
        options.implicitReferences = false;
        options.parameterRegisters = true;
        options.localsDirective = true;
        options.sequentialLabels = true;
        options.debugInfo = mBakDeb;
        options.codeOffsets = false;
        options.accessorComments = false;
        options.registerInfo = 0;
        options.inlineResolver = null;

        // Set jobs automatically.
        int jobs = Runtime.getRuntime().availableProcessors();
        if (jobs > 6) {
            jobs = 6;
        }

        DexBackedDexFile dexFile = dexEntry.getDexFile();

        if (dexFile.supportsOptimizedOpcodes()) {
            throw new AndrolibException("Could not disassemble an odex file without deodexing it.");
        }

        if (dexFile instanceof DexBackedOdexFile) {
            options.inlineResolver = InlineMethodResolver.createInlineMethodResolver(
                ((DexBackedOdexFile) dexFile).getOdexVersion());
        }

        Baksmali.disassembleDexFile(dexFile, outDir, jobs, options);

        return dexFile;
    }

    private boolean isDexContainerFormat(MultiDexContainer<? extends DexBackedDexFile> container) throws IOException {
        return mDexName.equals("classes.dex") && container.getDexEntryNames().size() > 1
                && container.getDexEntryNames().get(1).contains("/");
    }
}

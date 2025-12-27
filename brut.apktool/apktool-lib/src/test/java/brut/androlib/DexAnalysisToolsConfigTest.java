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
import static org.junit.Assert.*;

/**
 * Tests for DEX analysis tools configuration
 */
public class DexAnalysisToolsConfigTest {

    @Test
    public void testUseAnalysisToolsDefaultValue() {
        Config config = new Config("test-version");
        assertFalse("useAnalysisTools should be false by default", config.isUseAnalysisTools());
    }

    @Test
    public void testUseAnalysisToolsCanBeEnabled() {
        Config config = new Config("test-version");
        config.setUseAnalysisTools(true);
        assertTrue("useAnalysisTools should be true after being set", config.isUseAnalysisTools());
    }

    @Test
    public void testUseAnalysisToolsCanBeDisabled() {
        Config config = new Config("test-version");
        config.setUseAnalysisTools(true);
        config.setUseAnalysisTools(false);
        assertFalse("useAnalysisTools should be false after being disabled", config.isUseAnalysisTools());
    }
}

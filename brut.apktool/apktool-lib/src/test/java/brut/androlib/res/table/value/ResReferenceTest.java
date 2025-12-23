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
package brut.androlib.res.table.value;

import brut.androlib.exceptions.AndrolibException;
import brut.androlib.res.table.ResId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResReferenceTest {

    @Test
    public void testEncodeUnresolvedResourceReference() throws AndrolibException {
        // Test that an unresolved resource reference preserves its ID
        // instead of returning "@null"
        ResId id = ResId.of(0x580f0000);
        ResReference ref = new ResReference(null, id);
        
        String encoded = ref.encodeAsResXmlValue();
        
        // Should return @0x580f0000 instead of @null
        assertEquals("@0x580f0000", encoded);
    }

    @Test
    public void testEncodeUnresolvedAttributeReference() throws AndrolibException {
        // Test that an unresolved attribute reference preserves its ID
        ResId id = ResId.of(0x580f0000);
        ResReference ref = new ResReference(null, id, ResReference.Type.ATTRIBUTE);
        
        String encoded = ref.encodeAsResXmlValue();
        
        // Should return ?0x580f0000 instead of @null
        assertEquals("?0x580f0000", encoded);
    }

    @Test
    public void testEncodeNullReference() throws AndrolibException {
        // Test that NULL reference returns "@null"
        ResReference ref = ResReference.NULL;
        
        String encoded = ref.encodeAsResXmlValue();
        
        assertEquals("@null", encoded);
    }

    @Test
    public void testEncodeZeroReference() throws AndrolibException {
        // Test that a reference with ID 0 (ResId.NULL) returns "@null"
        ResReference ref = new ResReference(null, ResId.NULL);
        
        String encoded = ref.encodeAsResXmlValue();
        
        assertEquals("@null", encoded);
    }

    @Test
    public void testEncodeReferenceWithName() throws AndrolibException {
        // Test that when name is provided, it takes precedence
        ResId id = ResId.of(0x580f0000);
        ResReference ref = new ResReference(null, id, "@string/my_string");
        
        String encoded = ref.encodeAsResXmlValue();
        
        // Should return the name, not the ID
        assertEquals("@string/my_string", encoded);
    }
}

/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.spi.assetlinks.loader;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.AuthenticationDomain;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.Set;

import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link AssetRelationReader}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AssetRelationReaderTest {

    public static final AuthenticationDomain WEB_WWW_EXAMPLE_COM =
            new AuthenticationDomain("https://www.example.com");
    public static final AuthenticationDomain WEB_EXAMPLE_COM =
            new AuthenticationDomain("https://example.com");
    public static final AuthenticationDomain ANDROID_COM_EXAMPLE_APP =
            new AuthenticationDomain("android://sha256:8P1sW0EPJcslw7UzRsiXL64w-O50Ed-RBICtay1g24M=" +
                    "@com.example.app");

    @Test
    public void emptyStatementList() throws Exception {
        Set<AuthenticationDomain> targets = getCredentialSharingTargets("empty.json");
        assertTrue(targets.isEmpty());
    }

    @Test
    public void singleWebTarget() throws Exception {
        Set<AuthenticationDomain> targets = getCredentialSharingTargets("single_web.json");
        assertTrue(targets.size() == 1);
        assertTrue(targets.contains(WEB_WWW_EXAMPLE_COM));
    }

    @Test
    public void singleAndroidTarget() throws Exception {
        Set<AuthenticationDomain> targets = getCredentialSharingTargets("single_android.json");
        assertTrue(targets.size() == 1);
        assertTrue(targets.contains(ANDROID_COM_EXAMPLE_APP));
    }

    @Test
    public void ignoreUrlHandlingTargets() throws Exception {
        Set<AuthenticationDomain> targets = getCredentialSharingTargets("common_url_sharing.json");
        assertTrue(targets.isEmpty());
    }

    @Test
    public void multipleMixedTargets() throws Exception {
        Set<AuthenticationDomain> targets =
                getCredentialSharingTargets("multiple_mixed_targets.json");
        assertTrue(targets.size() == 3);
        assertTrue(targets.contains(WEB_WWW_EXAMPLE_COM));
        assertTrue(targets.contains(ANDROID_COM_EXAMPLE_APP));
        assertTrue(targets.contains(WEB_EXAMPLE_COM));
    }

    @Test(expected = JSONException.class)
    public void invalid_rootIsNotArray() throws Exception {
        getCredentialSharingTargets("invalid_top_level_object.json");
    }

    @Test(expected = JSONException.class)
    public void invalidStatement_missingNamespace() throws Exception {
        getCredentialSharingTargets("invalid_missing_namespace.json");
    }

    @Test(expected = JSONException.class)
    public void invalidStatement_web_missingSiteField() throws Exception {
        getCredentialSharingTargets("invalid_web_missing_site.json");
    }

    @Test(expected = JSONException.class)
    public void invalidStatement_android_missingPackageName() throws Exception {
        getCredentialSharingTargets("invalid_android_missing_package_name.json");
    }

    @Test(expected = JSONException.class)
    public void invalidStatement_android_missingFingerprint() throws Exception {
        getCredentialSharingTargets("invalid_android_missing_fingerprint.json");
    }

    @Test(expected = JSONException.class)
    public void invalidStatement_android_wrongFingerprintLength() throws Exception {
        getCredentialSharingTargets("invalid_fingerprint_wrong_length.json");
    }

    private Set<AuthenticationDomain> getCredentialSharingTargets(String fileName)
            throws Exception {
        return AssetRelationReader.getRelations(
                readTestData(fileName),
                AssetRelationTypes.GET_LOGIN_CREDS);
    }

    private String readTestData(String fileName) throws IOException {
        InputStream is = null;
        Scanner scanner = null;
        try {
            // read the whole file into a string, using a Scanner hack
            is = new FileInputStream("./testassetlinks/" + fileName);
            scanner = new Scanner(is);
            scanner.useDelimiter("\\Z");
            return scanner.next();
        } finally {
            if (scanner != null) {
                scanner.close();
            }

            if (is != null) {
                is.close();
            }
        }
    }
}

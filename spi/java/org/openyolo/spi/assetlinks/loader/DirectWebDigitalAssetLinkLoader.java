/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.spi.assetlinks.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.json.JSONException;
import org.openyolo.protocol.AuthenticationDomain;

/**
 * Retrieves and interprets digital asset link relations for web authentication domains, directly
 * from the domain's "/.well-known/assetlinks.json" declaration.
 */
public class DirectWebDigitalAssetLinkLoader implements DigitalAssetLinkLoader {

    @Override
    public Set<AuthenticationDomain> getRelations(
            String relationType,
            AuthenticationDomain domain)
            throws IOException {

        String assetLinksDeclaration;
        if (!domain.isWebAuthDomain()) {
            throw new IllegalArgumentException("WebDigitalAssetLinkLoader only supports "
                    + "loading relations for web authentication domains");
        }

        assetLinksDeclaration = loadFromWeb(domain.toString());

        try {
            return AssetRelationReader.getRelations(assetLinksDeclaration, relationType);
        } catch (JSONException ex) {
            throw new IOException("Unable to parse asset links", ex);
        }
    }

    private String loadFromWeb(String webDomain) throws IOException {
        InputStream stream = new URL(webDomain).openStream();
        return StreamReader.readAll(stream);
    }
}

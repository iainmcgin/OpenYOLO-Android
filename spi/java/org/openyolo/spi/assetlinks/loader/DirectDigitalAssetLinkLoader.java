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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.json.JSONException;
import org.openyolo.protocol.AuthenticationDomain;

/**
 * Retrieves and interprets digital asset link relations directly from the specified authentication
 * domain.
 */
public class DirectDigitalAssetLinkLoader implements DigitalAssetLinkLoader {

    private static final String TAG = "DirectDalLoader";
    private static final String ASSET_STATEMENTS = "asset_statements";
    private static final String EMPTY_ASSET_STATEMENT_LIST = "[]";

    private Context mApplicationContext;

    /**
     * Creates an asset link loader, using the provided context for Android app relation retrieval.
     */
    public DirectDigitalAssetLinkLoader(Context context) {
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public Set<AuthenticationDomain> getRelations(
            String relationType,
            AuthenticationDomain domain)
            throws IOException {

        String assetLinksDeclaration;
        if (domain.isWebAuthDomain()) {
            assetLinksDeclaration = loadFromWeb(domain.toString());
        } else {
            checkAppFingerprint(domain);
            assetLinksDeclaration = loadFromApp(domain.getAndroidPackageName());
        }

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

    private String loadFromApp(String packageName) throws IOException {
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = mApplicationContext.getPackageManager()
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException ex) {
            throw new IOException(
                    "Unable to find application info for package: " + packageName,
                    ex);
        }

        if (applicationInfo.metaData == null
                || !applicationInfo.metaData.containsKey(ASSET_STATEMENTS)) {
            return EMPTY_ASSET_STATEMENT_LIST;
        }

        Resources resources;
        try {
            resources = mApplicationContext.getPackageManager()
                    .getResourcesForApplication(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return EMPTY_ASSET_STATEMENT_LIST;
        }

        return resources.getString(applicationInfo.metaData.getInt(ASSET_STATEMENTS));
    }

    private void checkAppFingerprint(AuthenticationDomain androidApp) throws IOException {
        AuthenticationDomain appDomain =
                AuthenticationDomain.fromPackageName(
                        mApplicationContext,
                        androidApp.getAndroidPackageName());
        if (!androidApp.equals(appDomain)) {
            throw new IOException("Installed application does not match domain: " + androidApp);
        }
    }
}

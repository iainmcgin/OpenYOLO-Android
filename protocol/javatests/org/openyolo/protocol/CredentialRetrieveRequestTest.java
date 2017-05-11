/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openyolo.protocol;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.os.Parcel;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link CredentialRetrieveRequest}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialRetrieveRequestTest {

    @Test
    public void build_validInputs_shouldSucceed() {
        CredentialRetrieveRequest request = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
                .setTokenProviders(TestConstants.createTokenProviderMap())
                .setAdditionalProperties(TestConstants.ADDITIONAL_PROPS)
                .build();

        assertThat(request.getAuthenticationMethods()).hasSize(1);
        assertThat(request.getAuthenticationMethods()).contains(AuthenticationMethods.EMAIL);
        TestConstants.checkTokenProviderMap(request.getTokenProviders());
        TestConstants.checkAdditionalProps(request.getAdditionalProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderUriSetConstructor_withEmptySet_throwsIllegalArgumentException() {
        new CredentialRetrieveRequest.Builder(new HashSet<AuthenticationMethod>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderProtoConstructor_withNull_throwsIllegalArgumentException() {
        new CredentialRetrieveRequest.Builder((Protobufs.CredentialRetrieveRequest) null);
    }

    @Test
    public void testWriteAndRead() {
        CredentialRetrieveRequest request = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
                .setTokenProviders(TestConstants.createTokenProviderMap())
                .setAdditionalProperties(TestConstants.ADDITIONAL_PROPS)
                .build();

        Parcel p = Parcel.obtain();
        try {
            request.writeToParcel(p, 0);
            p.setDataPosition(0);
            CredentialRetrieveRequest deserialized = CredentialRetrieveRequest.CREATOR.createFromParcel(p);
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getAuthenticationMethods())
                    .isEqualTo(request.getAuthenticationMethods());
            TestConstants.checkTokenProviderMap(deserialized.getTokenProviders());
            TestConstants.checkAdditionalProps(deserialized.getAdditionalProperties());
        } finally {
            p.recycle();
        }
    }

    @Test
    public void forAuthenticationMethods_withValidAuthenticationMethodsUsingVarArgs_returnsValidRequest() {
        CredentialRetrieveRequest request = CredentialRetrieveRequest
            .forAuthenticationMethods(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);

        assertThat(request.getAuthenticationMethods())
            .containsOnly(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);
    }

    @Test
    public void forAuthenticationMethods_withValidAuthenticationMethodUsingSet_returnsValidRequest() {
        Set<AuthenticationMethod> authenticationMethods = new HashSet<>();
        Collections.addAll(
            authenticationMethods,
            AuthenticationMethods.GOOGLE,
            AuthenticationMethods.FACEBOOK);

        CredentialRetrieveRequest request = CredentialRetrieveRequest.forAuthenticationMethods(authenticationMethods);

        assertThat(request.getAuthenticationMethods())
            .containsOnly(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);
    }

    @Test
    public void testToProtocolBuffer_includesClientVersion() {
        String vendor = "test";
        int major = 1;
        int minor = 2;
        int patch = 3;
        ClientVersionUtil.setClientVersion(
                Protobufs.ClientVersion.newBuilder()
                        .setVendor(vendor)
                        .setMajor(major)
                        .setMinor(minor)
                        .setPatch(patch)
                        .build());

        try {
            CredentialRetrieveRequest request = CredentialRetrieveRequest.forAuthenticationMethods(
                    AuthenticationMethods.EMAIL);
            Protobufs.CredentialRetrieveRequest proto = request.toProtocolBuffer();
            assertThat(proto.hasClientVersion());
            assertThat(proto.getClientVersion().getVendor()).isEqualTo(vendor);
            assertThat(proto.getClientVersion().getMajor()).isEqualTo(major);
            assertThat(proto.getClientVersion().getMinor()).isEqualTo(minor);
            assertThat(proto.getClientVersion().getPatch()).isEqualTo(patch);
        } finally {
            ClientVersionUtil.setClientVersion(null);
        }
    }

    private static <T, U> void assertMapsEqual(Map<T, U> a, Map<T, U> b) {
        assertThat(a.keySet()).isEqualTo(b.keySet());
        for (T key : a.keySet()) {
            assertThat(a.get(key)).isEqualTo(b.get(key));
        }
    }
}
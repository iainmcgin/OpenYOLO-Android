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

package org.openyolo.protocol;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.internal.AuthenticationDomainConverters;
import org.openyolo.protocol.internal.CollectionConverter;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/**
 * Unit tests for {@link AuthenticationDomain}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AuthenticationDomainTest {

    private static final String WEB_AUTH_DOMAIN_STR = "https://www.example.com";

    private static final String VALID_PACKAGE_NAME = "com.example.app";

    private static byte[] VALID_SIGNATURE_BYTES = new byte[]
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

    private static final String VALID_FINGERPRINT =
            "2qKVvu1OLulMJAFbVq9ia08h759E8rPUD8QckJAKa_G0hnxDxXzaVNG2_Uhps_I8"
                    + "7V4Lo8BdCxaA307H0HYkAw==";

    private static final String VALID_AUTHENTICATION_DOMAIN_STRING =
            "android://" + VALID_FINGERPRINT + "@" + VALID_PACKAGE_NAME;

    private static final PackageInfo VALID_PACKAGE_INFO = makeValidPackageInfo();

    private static PackageInfo makeValidPackageInfo() {
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.signatures = new Signature[2];
        packageInfo.signatures[0] = new Signature(VALID_SIGNATURE_BYTES);
        packageInfo.signatures[1] = new Signature(new byte[16]);

        return packageInfo;
    }

    @Mock
    PackageManager mockPackageManager;
    @Mock
    Context mockContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockPackageManager.getPackageInfo(VALID_PACKAGE_NAME, PackageManager.GET_SIGNATURES))
                .thenReturn(VALID_PACKAGE_INFO);
        when(mockContext.getPackageName()).thenReturn(VALID_PACKAGE_NAME);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
    }

    @Test
    public void getSelfAuthDomain_withValidInput_returnsValidDomain() throws Exception {
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);

        assertThat(authDomain).isNotNull();
        assertThat(authDomain.isAndroidAuthDomain()).isTrue();
        assertThat(authDomain.toString())
                .isEqualTo(VALID_AUTHENTICATION_DOMAIN_STRING);
    }

    @Test
    public void listForPackage_forValidPackage_containsValidAuthenticationDomain() {
        assertThat(AuthenticationDomain.listForPackage(mockContext, VALID_PACKAGE_NAME))
                .contains(new AuthenticationDomain(VALID_AUTHENTICATION_DOMAIN_STRING));
    }

    @Test
    public void listForPackage_forNullPackageName_returnsEmpty() throws Exception {
        assertThat(AuthenticationDomain.listForPackage(mockContext, null /* package */)).isEmpty();
    }

    @Test
    public void listForPackage_packageDoesNotExist_returnsEmpty() throws Exception {
        when(mockPackageManager.getPackageInfo(VALID_PACKAGE_NAME, PackageManager.GET_SIGNATURES))
                .thenAnswer(new Answer<PackageInfo>() {
                    @Override
                    public PackageInfo answer(InvocationOnMock invocation) throws Throwable {
                        throw new NameNotFoundException();
                    }
                });

        assertThat(AuthenticationDomain.listForPackage(mockContext, VALID_PACKAGE_NAME)).isEmpty();
    }

    @Test(expected = RequireViolation.class)
    public void CreateAndroidAuthDomain_withNullDomain_throwsRequireViolation() throws Exception{
        AuthenticationDomain.createAndroidAuthDomain(null /* authDomain */, mock(Signature.class));
    }

    @Test
    public void validWebAuthDomain_hasExpectedBehaviour() {
        AuthenticationDomain authDomain = new AuthenticationDomain("https://www.example.com");

        assertThat(authDomain.isAndroidAuthDomain()).isFalse();
        assertThat(authDomain.isWebAuthDomain()).isTrue();

        try {
            authDomain.getAndroidPackageName();

            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ex) {}
    }

    @Test
    public void validAndroidAuthDomain_hasExpectedBehaviour(){
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);

        assertThat(authDomain.isAndroidAuthDomain()).isTrue();
        assertThat(authDomain.isWebAuthDomain()).isFalse();

        assertThat(authDomain.getAndroidPackageName()).isEqualTo(VALID_PACKAGE_NAME);
    }

    @Test
    public void getSelfAuthDomain_packageNotFoundException_throwsException() throws Exception {
        when(mockPackageManager.getPackageInfo(VALID_PACKAGE_NAME, PackageManager.GET_SIGNATURES))
            .thenAnswer(new Answer<PackageInfo>() {
                @Override
                public PackageInfo answer(InvocationOnMock invocation) throws Throwable {
                    throw new NameNotFoundException();
                }
            });

        try {
            AuthenticationDomain.getSelfAuthDomain(mockContext);

            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ex) {}
    }

    @Test
    public void stringsToAuthDomainsConverter_withValidInput_returnsExpectValues()
            throws Exception {
        List<String> domains = new ArrayList<>();
        domains.add("https://www.google.com");
        domains.add("https://www.yahoo.com");
        domains.add("https://www.facebook.com");

        List<AuthenticationDomain> result =
                CollectionConverter.toList(
                        domains,
                        AuthenticationDomainConverters.STRING_TO_DOMAIN);

        assertThat(result.size()).isEqualTo(3);

        assertThat(result.get(0).toString()).isEqualTo(domains.get(0));
        assertThat(result.get(1).toString()).isEqualTo(domains.get(1));
        assertThat(result.get(2).toString()).isEqualTo(domains.get(2));
    }

    @Test
    public void equals_withEquivalent_returnsTrue() throws Exception {
        AuthenticationDomain authDomainA = AuthenticationDomain.getSelfAuthDomain(mockContext);
        AuthenticationDomain authDomainB = AuthenticationDomain.getSelfAuthDomain(mockContext);

        assertThat(authDomainA).isEqualTo(authDomainA);
        assertThat(authDomainA).isEqualTo(authDomainB);

        assertThat(authDomainA.compareTo(authDomainB)).isEqualTo(0);
    }

    @Test
    public void equals_withDifferentType_returnsFalse() throws Exception {
        AuthenticationDomain domain = new AuthenticationDomain(WEB_AUTH_DOMAIN_STR);
        assertThat(domain).isNotEqualTo(WEB_AUTH_DOMAIN_STR);
    }

    @Test
    public void testHashCode_equivalent() throws Exception {
        AuthenticationDomain authDomainA = new AuthenticationDomain(WEB_AUTH_DOMAIN_STR);
        AuthenticationDomain authDomainB = new AuthenticationDomain(WEB_AUTH_DOMAIN_STR);

        assertThat(authDomainA.hashCode()).isEqualTo(authDomainB.hashCode());
    }

}

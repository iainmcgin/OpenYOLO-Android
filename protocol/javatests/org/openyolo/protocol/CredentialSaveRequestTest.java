/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
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
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_ANOTHER_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_STRING_VALUE;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_TEST_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_TWO_BYTE_VALUE;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_ZERO_BYTE_VALUE;

import android.os.Parcel;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidProperties;
import org.openyolo.protocol.TestConstants.ValidFacebookCredential;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.openyolo.protocol.internal.CollectionConverter;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link CredentialSaveRequest} */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class CredentialSaveRequestTest {

    private static final class ValidClientVersion {
        public static final int MAJOR = 1;
        public static final int MINOR = 2;
        public static final int PATCH = 3;
        public static final String VENDOR = "com.customVendor";

        public static final Protobufs.ClientVersion INSTANCE = Protobufs.ClientVersion.newBuilder()
                .setMajor(MAJOR)
                .setMinor(MINOR)
                .setPatch(PATCH)
                .setVendor(VENDOR)
                .build();
    }

    private static final class ValidRequest {
        private static final Credential CREDENTIAL = ValidFacebookCredential.make();
        private static final Map<String, byte[]> ADDITIONAL_PROPERTIES = ValidProperties.MAP_INSTANCE;

        private static final CredentialSaveRequest INSTANCE =
                new CredentialSaveRequest.Builder(CREDENTIAL)
                        .setAdditionalProperties(ADDITIONAL_PROPERTIES)
                        .build();

        public static void assertEqualTo(CredentialSaveRequest request) {
            ValidFacebookCredential.assertEqualTo(request.getCredential());
            ValidProperties.assertEqualTo(request.getAdditionalProperties());
        }

        public static void assertEqualTo(Protobufs.CredentialSaveRequest request) throws Exception {
            ValidFacebookCredential.assertEqualTo(request.getCredential());
            assertThat(request.getClientVersion()).isEqualTo(ValidClientVersion.INSTANCE);

            final Map<String, byte[]> additionalProperties =
                    CollectionConverter.convertMapValues(
                            request.getAdditionalPropsMap(),
                            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);

            ValidProperties.assertEqualTo(additionalProperties);
        }
    }

    @Before
    public void setup() {
        ClientVersionUtil.setClientVersion(ValidClientVersion.INSTANCE);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtoBytes_withNull_throwsMalformedDataException() throws Exception {
        CredentialSaveRequest.fromProtoBytes(null /* bytes */);
    }

    @Test()
    public void fromProtoBytes_withValidRequestBytes_returnsEquivalentRequest() throws Exception {
        final byte[] encodedRequest = ValidRequest.INSTANCE.toProtocolBuffer().toByteArray();

        final CredentialSaveRequest request = CredentialSaveRequest.fromProtoBytes(encodedRequest);

        ValidRequest.assertEqualTo(request);
    }

    @Test
    public void fromProto_withValidProto_resultIsEqual() throws Exception {
        final CredentialSaveRequest request =
            CredentialSaveRequest.fromProtobuf(ValidRequest.INSTANCE.toProtocolBuffer());

        ValidRequest.assertEqualTo(request);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProto_withNullProto_throwsMalformedDataException() throws Exception {
        CredentialSaveRequest.fromProtobuf(null);
    }

    @Test
    public void fromCredential_withValidCredential_returnsRequestForCredential() {
        final CredentialSaveRequest request =
                CredentialSaveRequest.fromCredential(ValidFacebookCredential.make());

        ValidFacebookCredential.assertEqualTo(request.getCredential());
        assertThat(request.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void parcelAndUnparcel_withValidCredential_isEqual() {
        Parcel parcel = Parcel.obtain();
        ValidRequest.INSTANCE.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final CredentialSaveRequest request =
                CredentialSaveRequest.CREATOR.createFromParcel(parcel);

        ValidRequest.assertEqualTo(request);
    }

    @Test
    public void toProtocolBuffer_withValidCredential_containsClientVersion() throws Exception {
        final Protobufs.CredentialSaveRequest protoRequest =
                ValidRequest.INSTANCE.toProtocolBuffer();

        ValidRequest.assertEqualTo(protoRequest);
    }

    @Test
    public void builder_withValidCredential_resultIsEqual() {
        final CredentialSaveRequest request =
                new CredentialSaveRequest.Builder(ValidRequest.CREDENTIAL).build();

        assertThat(request.getCredential()).isSameAs(ValidRequest.CREDENTIAL);
        assertThat(request.getAdditionalProperties()).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_withNullCredential_throwsIllegalArgumentException() {
        new CredentialSaveRequest.Builder((Credential) null);
    }

    public void builder_setAdditionalPropertiesWithNull_returnsEmptyMap() {
        CredentialSaveRequest request =
                new CredentialSaveRequest.Builder(ValidRequest.CREDENTIAL)
                        .setAdditionalProperties(ValidRequest.ADDITIONAL_PROPERTIES)
                        .setAdditionalProperties(null)
                        .build();

        assertThat(request.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void builder_setAdditionalProperties_isSuccessful() {
        final CredentialSaveRequest request =
                new CredentialSaveRequest.Builder(ValidRequest.CREDENTIAL)
                        .setAdditionalProperties(ValidRequest.ADDITIONAL_PROPERTIES)
                        .build();

        ValidProperties.assertEqualTo(request.getAdditionalProperties());
    }

    @Test
    public void testBuilder_setAdditionalProperty() {
        CredentialSaveRequest csr = new CredentialSaveRequest.Builder(
                ValidRequest.CREDENTIAL)
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE)
                .setAdditionalProperty(ADDITIONAL_PROP_ANOTHER_KEY, ADDITIONAL_PROP_ZERO_BYTE_VALUE)
                .build();

        Map<String, byte[]> additionalProps = csr.getAdditionalProperties();
        assertThat(additionalProps.size()).isEqualTo(2);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_TWO_BYTE_VALUE);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_ANOTHER_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_ANOTHER_KEY))
                .isEqualTo(ADDITIONAL_PROP_ZERO_BYTE_VALUE);
    }

    @Test
    public void testBuilder_setAdditionalProperty_overwriteExistingValue() {
        CredentialSaveRequest csr = new CredentialSaveRequest.Builder(
                ValidRequest.CREDENTIAL)
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE)
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_ZERO_BYTE_VALUE)
                .build();

        Map<String, byte[]> additionalProps = csr.getAdditionalProperties();
        assertThat(additionalProps.size()).isEqualTo(1);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_ZERO_BYTE_VALUE);
    }

    @Test
    public void testBuilder_setAdditionalPropertyAsString() {
        CredentialSaveRequest csr = new CredentialSaveRequest.Builder(
                ValidRequest.CREDENTIAL)
                .setAdditionalPropertyAsString(
                        ADDITIONAL_PROP_TEST_KEY,
                        ADDITIONAL_PROP_STRING_VALUE)
                .build();

        Map<String, byte[]> additionalProps = csr.getAdditionalProperties();
        assertThat(additionalProps.size()).isEqualTo(1);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(AdditionalPropertiesHelper.encodeStringValue(
                        ADDITIONAL_PROP_STRING_VALUE));
    }

    @Test
    public void testGetAdditionalProperty() {
        CredentialSaveRequest csr = new CredentialSaveRequest.Builder(
                ValidRequest.CREDENTIAL)
                .setAdditionalProperties(ImmutableMap.of(
                        ADDITIONAL_PROP_TEST_KEY,
                        ADDITIONAL_PROP_TWO_BYTE_VALUE))
                .build();

        assertThat(csr.getAdditionalProperty(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_TWO_BYTE_VALUE);
    }

    @Test
    public void testGetAdditionalProperty_withMissingKey() {
        CredentialSaveRequest csr = new CredentialSaveRequest.Builder(
                ValidRequest.CREDENTIAL)
                .build();
        assertThat(csr.getAdditionalProperty("missingKey")).isNull();
    }

    @Test
    public void testGetAdditionalPropertyAsString() {
        CredentialSaveRequest csr = new CredentialSaveRequest.Builder(
                ValidRequest.CREDENTIAL)
                .setAdditionalProperties(ImmutableMap.of(
                        ADDITIONAL_PROP_TEST_KEY,
                        AdditionalPropertiesHelper.encodeStringValue(ADDITIONAL_PROP_STRING_VALUE)))
                .build();

        assertThat(csr.getAdditionalPropertyAsString(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_STRING_VALUE);
    }
}

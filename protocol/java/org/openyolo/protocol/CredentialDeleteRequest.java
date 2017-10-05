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

package org.openyolo.protocol;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.protocol.internal.AdditionalPropertiesUtil.validateAdditionalProperties;
import static org.openyolo.protocol.internal.AdditionalPropertiesUtil.validateAdditionalPropertiesFromProto;
import static org.valid4j.Validation.validate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.AdditionalPropertiesUtil;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.openyolo.protocol.internal.IntentProtocolBufferExtractor;

/**
 * A request to delete a credential, to be sent to a credential provider on the device.
 *
 * @see <a href="http://spec.openyolo.org/openyolo-android-spec.html#credential-deletion">
 *     OpenYOLO Specification: Credential Deletion</a>
 */
public final class CredentialDeleteRequest implements AdditionalPropertiesContainer {

    /**
     * Returns a deletion request for the given credential.
     */
    public static CredentialDeleteRequest fromCredential(Credential credential) {
        return new Builder(credential).build();
    }

    /**
     * Creates a credential deletion request from its protocol buffer equivalent, in byte array
     * form.
     * @throws MalformedDataException if the given protocol buffer is invalid.
     */
    public static CredentialDeleteRequest fromProtobufBytes(@NonNull byte[] protobufBytes)
            throws MalformedDataException {
        validate(protobufBytes, notNullValue(), MalformedDataException.class);

        try {
            return fromProtobuf(Protobufs.CredentialDeleteRequest.parseFrom(protobufBytes));
        } catch (InvalidProtocolBufferException ex) {
            throw new MalformedDataException("unable to parse credential deletion request", ex);
        }
    }

    /**
     * Creates a credential deletion request from its protocol buffer equivalent.
     * @throws MalformedDataException if the given protocol buffer is invalid.
     */
    public static CredentialDeleteRequest fromProtobuf(
            @NonNull Protobufs.CredentialDeleteRequest proto)
            throws MalformedDataException {
        validate(proto, notNullValue(), MalformedDataException.class);

        return new Builder(proto).build();
    }


    /**
     * Extracts a credential deletion request from a request intent.
     * @throws MalformedDataException if the provided intent is null, is missing the byte array
     *     extra for the request, or the request contains invalid data.
     */
    public static CredentialDeleteRequest fromRequestIntent(
            @Nullable Intent requestIntent)
            throws MalformedDataException {
        return fromProtobuf(
                IntentProtocolBufferExtractor.extract(
                        ProtocolConstants.EXTRA_DELETE_REQUEST,
                        Protobufs.CredentialDeleteRequest.parser(),
                        "deletion request missing or contains invalid data",
                        requestIntent));
    }

    @NonNull
    private final Credential mCredential;

    @NonNull
    private final Map<String, ByteString> mAdditionalProps;

    private CredentialDeleteRequest(Builder builder) {
        mCredential = builder.mCredential;
        mAdditionalProps = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * The credential to be deleted.
     */
    @NonNull
    public Credential getCredential() {
        return mCredential;
    }

    @Override
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return AdditionalPropertiesUtil.convertValuesToByteArrays(mAdditionalProps);
    }

    @Override
    @Nullable
    public byte[] getAdditionalProperty(String key) {
        return AdditionalPropertiesUtil.getPropertyValue(mAdditionalProps, key);
    }

    @Override
    @Nullable
    public String getAdditionalPropertyAsString(String key) {
        return AdditionalPropertiesUtil.getPropertyValueAsString(mAdditionalProps, key);
    }

    /**
     * Converts the request to its protocol buffer equivalent.
     */
    public Protobufs.CredentialDeleteRequest toProtobuf() {
        return Protobufs.CredentialDeleteRequest.newBuilder()
                .setClientVersion(ClientVersionUtil.getClientVersion())
                .setCredential(mCredential.toProtobuf())
                .putAllAdditionalProps(mAdditionalProps)
                .build();
    }

    /**
     * Creates instances of {@link CredentialDeleteRequest}.
     */
    public static final class Builder
            implements AdditionalPropertiesBuilder<CredentialDeleteRequest, Builder> {

        @NonNull
        private Credential mCredential;

        @NonNull
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Starts the process of describing a credential deletion request, based on the properties
         * contained in the provided protocol buffer.
         */
        private Builder(Protobufs.CredentialDeleteRequest proto) throws MalformedDataException {
            try {
                setCredential(Credential.fromProtobuf(proto.getCredential()));
                setAdditionalPropertiesFromProto(proto.getAdditionalPropsMap());
            } catch (IllegalArgumentException ex) {
                throw new MalformedDataException(ex);
            }
        }

        /**
         * Starts the process of describing a credential deletion request, specifying the mandatory
         * credential object.
         */
        public Builder(@NonNull Credential credential) {
            setCredential(credential);
        }

        /**
         * Specifies the credential to be deleted. Must not be null.
         */
        public Builder setCredential(@NonNull Credential credential) {
            validate(credential, notNullValue(), IllegalArgumentException.class);

            mCredential = credential;
            return this;
        }

        @Override
        @NonNull
        public Builder setAdditionalProperties(
                @Nullable Map<String, byte[]> additionalProperties) {
            mAdditionalProps = validateAdditionalProperties(additionalProperties);
            return this;
        }

        @Override
        @NonNull
        public Builder setAdditionalProperty(@NonNull String key, @Nullable byte[] value) {
            AdditionalPropertiesUtil.setPropertyValue(mAdditionalProps, key, value);
            return this;
        }

        @Override
        @NonNull
        public Builder setAdditionalPropertyAsString(@NonNull String key, @Nullable String value) {
            AdditionalPropertiesUtil.setPropertyValueAsString(mAdditionalProps, key, value);
            return this;
        }

        private Builder setAdditionalPropertiesFromProto(
                Map<String, ByteString> additionalProperties) {
            mAdditionalProps = validateAdditionalPropertiesFromProto(additionalProperties);
            return this;
        }

        /**
         * Creates the credential deletion request, from the specified properties.
         */
        @Override
        @NonNull
        public CredentialDeleteRequest build() {
            return new CredentialDeleteRequest(this);
        }
    }
}

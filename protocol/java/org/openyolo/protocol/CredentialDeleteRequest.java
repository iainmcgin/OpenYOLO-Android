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
import static org.valid4j.Assertive.require;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.openyolo.protocol.internal.CollectionConverter;
import org.openyolo.protocol.internal.IntentProtocolBufferExtractor;
import org.valid4j.errors.RequireViolation;

/**
 * A request to delete a credential, to be sent to a credential provider on the device.
 */
public final class CredentialDeleteRequest {

    /**
     * Creates a credential deletion request from its protocol buffer equivalent.
     * @throws MalformedDataException if the protocol buffer is null or contains invalid data.
     */
    public static CredentialDeleteRequest fromProtobuf(
            @Nullable Protobufs.CredentialDeleteRequest proto)
            throws MalformedDataException {
        if (proto == null) {
            throw new MalformedDataException("credential delete proto is not defined");
        }

        try {
            return new Builder(proto).build();
        } catch (RequireViolation ex) {
            throw new MalformedDataException("credential deletion request contains invalid data");
        }
    }

    /**
     * Creates a credential deletion request from its protocol buffer equivalent, in byte array
     * form.
     * @throws MalformedDataException if the protocol buffer is null or cannot be parsed from the
     *     byte array.
     */
    public static CredentialDeleteRequest fromProtobufBytes(
            @Nullable byte[] protobufBytes)
            throws MalformedDataException {
        if (protobufBytes == null) {
            throw new MalformedDataException("credential deletion request not defined");
        }

        try {
            return fromProtobuf(Protobufs.CredentialDeleteRequest.parseFrom(protobufBytes));
        } catch (InvalidProtocolBufferException ex) {
            throw new MalformedDataException("unable to parse credential deletion request", ex);
        }
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

    /**
     * The additional, non-standard properties specified by the client as part of this
     * deletion request.
     */
    public Map<String, byte[]> getAdditionalProps() {
        return CollectionConverter.convertMapValues(
                mAdditionalProps,
                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
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
    public static final class Builder {

        @NonNull
        private Credential mCredential;

        @NonNull
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Starts the process of describing a credential deletion request, based on the properties
         * contained in the provided protocol buffer.
         */
        public Builder(Protobufs.CredentialDeleteRequest proto) {
            setCredentialFromProto(proto.getCredential());
            setAdditionalPropertiesFromProto(proto.getAdditionalPropsMap());
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
            require(credential, notNullValue());
            mCredential = credential;
            return this;
        }

        private Builder setCredentialFromProto(@Nullable Protobufs.Credential credentialProto) {
            require(credentialProto, notNullValue());
            setCredential(Credential.fromProtobuf(credentialProto));
            return this;
        }

        /**
         * Specifies the set of additional, non-standard properties to carry with the credential
         * deletion request. A null map is treated as an empty map.
         */
        public Builder setAdditionalProperties(
                @Nullable Map<String, byte[]> additionalProperties) {
            mAdditionalProps = validateAdditionalProperties(additionalProperties);
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
        public CredentialDeleteRequest build() {
            return new CredentialDeleteRequest(this);
        }
    }
}

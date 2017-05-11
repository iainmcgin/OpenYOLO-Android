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

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.protocol.internal.CustomMatchers.isHttpsUriStr;
import static org.openyolo.protocol.internal.CustomMatchers.notNullOrEmptyString;
import static org.valid4j.Assertive.require;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openyolo.protocol.internal.AuthenticationMethodConverters;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.openyolo.protocol.internal.CollectionConverter;
import org.openyolo.protocol.internal.NoopValueConverter;
import org.openyolo.protocol.internal.TokenRequestInfoConverters;

/**
 * A request for a login hint, to be sent to credential providers on the device. Hints provide
 * the basic set of information that can help with account creation, or re-discovering an
 * existing account.
 */
public class HintRetrieveRequest implements Parcelable {

    /**
     * Parcelable reader for {@link HintRetrieveRequest} instances.
     * @see android.os.Parcelable
     */
    public static final Creator<HintRetrieveRequest> CREATOR = new HintRequestCreator();

    @NonNull
    private final Set<AuthenticationMethod> mAuthMethods;

    @NonNull
    private final Map<String, TokenRequestInfo> mTokenProviders;

    @NonNull
    private final PasswordSpecification mPasswordSpec;

    @NonNull
    private final Map<String, ByteString> mAdditionalProperties;

    /**
     * Creates a hint request for email and password based accounts.
     */
    public static HintRetrieveRequest forEmailAndPasswordAccount() {
        return new HintRetrieveRequest.Builder(AuthenticationMethods.EMAIL)
                .build();
    }

    /**
     * Reads a hint request from its protocol buffer byte array form.
     * @throws IOException if the hint request could not be decoded.
     *
     * @see #toProtocolBuffer().
     */
    @NonNull
    public static HintRetrieveRequest fromProtoBytes(byte[] hintRequestProtoBytes)
            throws IOException {
        if (hintRequestProtoBytes == null) {
            throw new IOException("Unable to decode hint request from null array");
        }
        return new HintRetrieveRequest.Builder(
                Protobufs.HintRetrieveRequest.parseFrom(hintRequestProtoBytes))
                .build();
    }

    private HintRetrieveRequest(@NonNull Builder builder) {
        mAuthMethods = Collections.unmodifiableSet(builder.mAuthMethods);
        mTokenProviders = Collections.unmodifiableMap(builder.mTokenProviders);
        mPasswordSpec = builder.mPasswordSpec;
        mAdditionalProperties = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * The set of authentication methods supported by the client for login.
     */
    @NonNull
    public Set<AuthenticationMethod> getAuthenticationMethods() {
        return mAuthMethods;
    }

    /**
     * The token providers supported by the requester, with the per-provider request info
     * message.
     */
    @NonNull
    public Map<String, TokenRequestInfo> getTokenProviders() {
        return mTokenProviders;
    }

    /**
     * A password specification which describes the "shape" of passwords that the requester
     * allows. This can be used for password generation by the credential provider, to
     * facilitate the creation of accounts with secure, unique passwords across all of the
     * user's accounts.
     */
    @NonNull
    public PasswordSpecification getPasswordSpecification() {
        return mPasswordSpec;
    }

    /**
     * The map of additional, non-standard properties included with this request.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return CollectionConverter.convertMapValues(
                mAdditionalProperties,
                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
    }

    /**
     * Retrieves the value of the named additional parameter, where the value is a UTF-8
     * encoded string.
     */
    @Nullable
    public String getAdditionalPropertyAsString(@NonNull String key) {
        require(key, notNullValue());
        if (mAdditionalProperties.containsKey(key)) {
            return mAdditionalProperties.get(key).toString(Charset.forName("UTF-8"));
        }
        return null;
    }

    /**
     * Retrieves the raw, byte-array value of the named additional parameter.
     */
    @Nullable
    public byte[] getAdditionalProperty(@NonNull String key) {
        require(key, notNullValue());
        return mAdditionalProperties.get(key).toByteArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte[] protoBytes = toProtocolBuffer().toByteArray();
        dest.writeInt(protoBytes.length);
        dest.writeByteArray(protoBytes);
    }

    /**
     * Converts the request into a protocol buffer, for storage or transmission.
     */
    @NonNull
    public Protobufs.HintRetrieveRequest toProtocolBuffer() {
        return Protobufs.HintRetrieveRequest.newBuilder()
                .setClientVersion(ClientVersionUtil.getClientVersion())
                .addAllAuthMethods(
                        CollectionConverter.toList(
                                mAuthMethods,
                                AuthenticationMethodConverters.OBJECT_TO_PROTOBUF))
                .putAllSupportedTokenProviders(
                        CollectionConverter.convertMapValues(
                                mTokenProviders,
                                TokenRequestInfoConverters.OBJECT_TO_PROTOBUF))
                .putAllAdditionalProps(mAdditionalProperties)
                .setPasswordSpec(mPasswordSpec.toProtocolBuffer())
                .build();
    }

    /**
     * Creates {@link HintRetrieveRequest} instances.
     */
    public static final class Builder {

        private Set<AuthenticationMethod> mAuthMethods = new HashSet<>();
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();
        private Map<String, TokenRequestInfo> mTokenProviders = new HashMap<>();
        private PasswordSpecification mPasswordSpec = PasswordSpecification.DEFAULT;

        /**
         * Recreates a hint request from its protocol buffer form.
         *
         * @see HintRetrieveRequest#toProtocolBuffer()
         */
        public Builder(@NonNull Protobufs.HintRetrieveRequest requestProto) {
            require(requestProto, notNullValue());
            setAuthenticationMethods(CollectionConverter.toSet(
                    requestProto.getAuthMethodsList(),
                    AuthenticationMethodConverters.PROTOBUF_TO_OBJECT));
            setTokenProviders(CollectionConverter.convertMapValues(
                    requestProto.getSupportedTokenProvidersMap(),
                    TokenRequestInfoConverters.PROTOBUF_TO_OBJECT));
            setPasswordSpecification(
                    new PasswordSpecification.Builder(requestProto.getPasswordSpec()).build());
            setAdditionalProperties(
                    CollectionConverter.convertMapValues(
                            requestProto.getAdditionalPropsMap(),
                            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
        }

        /**
         * Starts the process of defining a hint request, specifying at least one supported
         * authentication method.
         *
         * @see AuthenticationMethods
         */
        public Builder(
                @NonNull AuthenticationMethod authMethod,
                @NonNull AuthenticationMethod... additionalAuthMethods) {
            setAuthenticationMethods(authMethod, additionalAuthMethods);
        }

        /**
         * Starts the process of defining a hint request, specifying at least one supported
         * authentication method in string form.
         *
         * @see AuthenticationMethods
         */
        public Builder(@NonNull Set<AuthenticationMethod> authMethods) {
            setAuthenticationMethods(authMethods);
        }

        /**
         * Specifies the authentication methods supported by the requester. At least one
         * authentication method must be specified.
         *
         * @see AuthenticationMethods
         */
        @NonNull
        public Builder setAuthenticationMethods(
                @NonNull AuthenticationMethod authMethod,
                AuthenticationMethod... additionalAuthMethods) {
            setAuthenticationMethods(
                    CollectionConverter.toSet(
                            authMethod,
                            additionalAuthMethods,
                            NoopValueConverter.<AuthenticationMethod>getInstance()));
            return this;
        }

        /**
         * Specifies the authentication methods supported by the requester. At least one
         * authentication method must be specified.
         *
         * @see AuthenticationMethods
         */
        @NonNull
        public Builder setAuthenticationMethods(@NonNull Set<AuthenticationMethod> authMethods) {
            require(authMethods, notNullValue());
            require(!authMethods.contains(null), "every authentication methods must be non-null");
            require(!authMethods.isEmpty(), "At least one authentication method must be specified");
            mAuthMethods = authMethods;
            return this;
        }

        /**
         * Adds an authentication method supported by the requester.
         *
         * @see AuthenticationMethods
         */
        @NonNull
        public Builder addAuthenticationMethod(@NonNull AuthenticationMethod authenticationMethod) {
            require(authenticationMethod, notNullValue());
            mAuthMethods.add(authenticationMethod);
            return this;
        }

        /**
         * Specifies the token providers supported by the requester, and any token provider
         * specific information required to acquire usable ID tokens. A null map is treated
         * as equivalent to an empty map. Keys must be valid https URIs. Null token request info
         * messages are treated as equivalent to the {@link TokenRequestInfo#DEFAULT default} token
         * request info message.
         */
        public Builder setTokenProviders(@Nullable Map<String, TokenRequestInfo> tokenProviders) {
            if (tokenProviders == null) {
                mTokenProviders.clear();
                return this;
            }

            for (Map.Entry<String, TokenRequestInfo> entry : tokenProviders.entrySet()) {
                addTokenProvider(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Adds a token provider supported by the requester, with the default token request info
         * message specified.
         */
        public Builder addTokenProvider(@NonNull String tokenProviderUri) {
            addTokenProvider(tokenProviderUri, TokenRequestInfo.DEFAULT);
            return this;
        }

        /**
         * Adds a token provider supported by the requester, with the specified token request info
         * message for token retrieval. A null token request info message is treated as equivalent
         * to the {@link TokenRequestInfo#DEFAULT default} token request info message.
         */
        public Builder addTokenProvider(
                @NonNull String tokenProviderUri,
                @Nullable TokenRequestInfo info) {
            require(tokenProviderUri, isHttpsUriStr());
            if (info == null) {
                info = TokenRequestInfo.DEFAULT;
            }

            mTokenProviders.put(tokenProviderUri, info);
            return this;
        }

        /**
         * Specifies additional, non-standard hint request parameters. The provided map
         * must be non-null, and contain only non-empty keys and non-null values.
         */
        @NonNull
        public Builder setAdditionalProperties(@NonNull Map<String, byte[]> additionalParams) {
            require(additionalParams, notNullValue());
            require(additionalParams.keySet(), everyItem(notNullOrEmptyString()));
            require(additionalParams.values(), everyItem(notNullValue()));
            mAdditionalProps = CollectionConverter.convertMapValues(
                    additionalParams,
                    ByteStringConverters.BYTE_ARRAY_TO_BYTE_STRING);
            return this;
        }

        /**
         * Adds an additional, non-standard parameter to the hint request with a string value, that
         * will be encoded as UTF-8. The parameter name and value must both be non-null, and the
         * key must be non-empty.
         */
        @NonNull
        public Builder addAdditionalProperty(@NonNull String name, @NonNull String value) {
            require(value, notNullValue());
            addAdditionalProperty(name, value.getBytes(Charset.forName("UTF-8")));
            return this;
        }

        /**
         * Adds an additional, non-standard parameter to the hint request, that will be encoded as
         * UTF-8. The parameter name and value must both be non-null, and the key must be non-empty.
         */
        @NonNull
        public Builder addAdditionalProperty(@NonNull String name, @NonNull byte[] value) {
            require(name, notNullOrEmptyString());
            require(value, notNullValue());
            mAdditionalProps.put(name, ByteString.copyFrom(value));
            return this;
        }

        /**
         * Specifies the "shape" of passwords that the requester supports. This will be used
         * for password generation by the credential provider. If no value is explicitly provided,
         * the {@link PasswordSpecification#DEFAULT default password specification} will be used.
         */
        @NonNull
        public Builder setPasswordSpecification(@NonNull PasswordSpecification passwordSpec) {
            require(passwordSpec, notNullValue());
            mPasswordSpec = passwordSpec;
            return this;
        }

        /**
         * Creates a {@link HintRetrieveRequest} with the properties specified on the builder.
         */
        @NonNull
        public HintRetrieveRequest build() {
            return new HintRetrieveRequest(this);
        }
    }

    private static final class HintRequestCreator implements Creator<HintRetrieveRequest> {
        @Override
        public HintRetrieveRequest createFromParcel(Parcel in) {

            int protoLength = in.readInt();
            byte[] protoBytes = new byte[protoLength];
            in.readByteArray(protoBytes);

            try {
                return new HintRetrieveRequest.Builder(
                        Protobufs.HintRetrieveRequest.parseFrom(protoBytes))
                        .build();
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to read proto from parcel", ex);
            }
        }

        @Override
        public HintRetrieveRequest[] newArray(int size) {
            return new HintRetrieveRequest[size];
        }
    }
}

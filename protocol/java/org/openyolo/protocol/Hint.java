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
import static org.openyolo.protocol.internal.CustomMatchers.isValidAuthenticationMethod;
import static org.openyolo.protocol.internal.CustomMatchers.isWebUri;
import static org.openyolo.protocol.internal.CustomMatchers.notNullOrEmptyString;
import static org.openyolo.protocol.internal.CustomMatchers.nullOr;
import static org.openyolo.protocol.internal.StringUtil.nullifyEmptyString;
import static org.valid4j.Assertive.require;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.AdditionalPropertiesUtil;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * A representation of a hint for use in account discovery or account creation. This provides a
 * higher-level, data-verifying wrapper for the underlying {@link Protobufs.Credential protocol
 * buffer} that is returned by the provider.
 *
 * <p>Hints must have an {@link #getIdentifier() identifier} and an
 * an {@link #getAuthenticationMethod() authentication method}, but all other properties are
 * optional.
 */
public final class Hint implements Parcelable {

    /**
     * Parcelable reader for {@link Hint} instances.
     * @see android.os.Parcelable
     */
    public static final Creator<Hint> CREATOR = new HintCreator();

    /**
     * Creates a hint from its protocol buffer equivalent.
     */
    public static Hint fromProtobuf(Protobufs.Hint proto) {
        return new Hint.Builder(proto).build();
    }

    /**
     * Creates a hint from its protocol buffer equivalent, in byte array form.
     * @throws IOException if the protocol buffer cannot be parsed from the byte array.
     */
    public static Hint fromProtobufBytes(byte[] protoBytes) throws IOException {
        return fromProtobuf(Protobufs.Hint.parseFrom(protoBytes));
    }

    @NonNull
    private final String mId;

    @NonNull
    private final AuthenticationMethod mAuthMethod;

    @Nullable
    private final String mDisplayName;

    @Nullable
    private final Uri mDisplayPictureUri;

    @Nullable
    private final String mGeneratedPassword;

    @Nullable
    private final String mIdToken;

    @NonNull
    private final Map<String, ByteString> mAdditionalProps;

    private Hint(Hint.Builder builder) {
        mId = builder.mId;
        mAuthMethod = builder.mAuthMethod;
        mDisplayName = builder.mDisplayName;
        mDisplayPictureUri = builder.mDisplayPictureUri;
        mGeneratedPassword = builder.mGeneratedPassword;
        mIdToken = builder.mIdToken;
        mAdditionalProps = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * Creates a protocol buffer representation of the hint, for transmission or storage.
     */
    public Protobufs.Hint toProtobuf() {
        Protobufs.Hint.Builder builder = Protobufs.Hint.newBuilder()
                .setId(mId)
                .setAuthMethod(mAuthMethod.toProtobuf())
                .putAllAdditionalProps(mAdditionalProps);

        if (mDisplayName != null) {
            builder.setDisplayName(mDisplayName);
        }

        if (mDisplayPictureUri != null) {
            builder.setDisplayPictureUri(mDisplayPictureUri.toString());
        }

        if (mGeneratedPassword != null) {
            builder.setGeneratedPassword(mGeneratedPassword);
        }

        if (mIdToken != null) {
            builder.setIdToken(mIdToken);
        }

        return builder.build();
    }

    /**
     * The hint identifier, for use in discovering an existing account within the context of the
     * authentication method and authentication domain (which is implicitly that of the requester),
     * or creating a new account.
     */
    @NonNull
    public String getIdentifier() {
        return mId;
    }

    /**
     * The authentication domain associated with the hint.
     */
    @NonNull
    public AuthenticationMethod getAuthenticationMethod() {
        return mAuthMethod;
    }

    /**
     * A suggested display name for accounts created using this hint, if available.
     */
    @Nullable
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * A suggested profile picture for accounts created using this hint. if available.
     */
    @Nullable
    public Uri getDisplayPictureUri() {
        return mDisplayPictureUri;
    }

    /**
     * A suggested password for use with accounts created using this hint, if available.
     */
    @Nullable
    public String getGeneratedPassword() {
        return mGeneratedPassword;
    }

    /**
     * An ID token that provides "proof of access" to the hint identifier, if available.
     * When provided this can be used either as a means of authentication in its
     * own right, or as a means to bypass manual proof of access verification, such as by sending
     * an email with a verification link or an SMS with a code.
     */
    @Nullable
    public String getIdToken() {
        return mIdToken;
    }

    /**
     * Additional, non-standard properties associated with the hint.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return CollectionConverter.convertMapValues(
                mAdditionalProps,
                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte[] hintBytes = toProtobuf().toByteArray();
        dest.writeInt(hintBytes.length);
        dest.writeByteArray(hintBytes);
    }

    /**
     * Creates instances of {@link Hint}.
     */
    public static final class Builder {

        @NonNull
        private String mId;

        @NonNull
        private AuthenticationMethod mAuthMethod;

        @Nullable
        private String mDisplayName;

        @Nullable
        private Uri mDisplayPictureUri;

        @Nullable
        private String mGeneratedPassword;

        @Nullable
        private String mIdToken;

        @NonNull
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Starts the process of creating a hint, with the mandatory properties specified.
         */
        public Builder(
                @NonNull String id,
                @NonNull AuthenticationMethod authMethod) {
            setIdentifier(id);
            setAuthMethod(authMethod);
        }

        /**
         * Starts the process of creating a hint, with the mandatory properties specified.
         */
        public Builder(
                @NonNull String id,
                @NonNull String authMethod) {
            this(id, new AuthenticationMethod(authMethod));
        }

        /**
         * Starts the process of creating a hint, based on the properties of the provided
         * protocol buffer representation of the credential. The protocol buffer must contain
         * valid data for a hint.
         */
        public Builder(
                @NonNull Protobufs.Hint proto) {
            require(proto, notNullValue());
            setIdentifier(proto.getId());
            setAuthMethod(proto.getAuthMethod().getUri());
            setDisplayName(proto.getDisplayName());
            setDisplayPictureUri(proto.getDisplayPictureUri());
            setGeneratedPassword(proto.getGeneratedPassword());
            setIdToken(proto.getIdToken());
            setAdditionalPropertiesFromProto(proto.getAdditionalPropsMap());
        }

        /**
         * Specifies the identifier for the hint. Must not be null or empty.
         */
        public Builder setIdentifier(@NonNull String id) {
            require(id, notNullOrEmptyString());
            mId = id;
            return this;
        }

        /**
         * Specifies the authentication method for the hint, in string form. Must be a valid
         * authentication method URI.
         *
         * @see AuthenticationMethod
         */
        public Builder setAuthMethod(@NonNull String authMethod) {
            require(authMethod, isValidAuthenticationMethod());
            mAuthMethod = new AuthenticationMethod(authMethod);
            return this;
        }

        /**
         * Specifies the authentication method for the hint. Must not be null.
         */
        public Builder setAuthMethod(@NonNull AuthenticationMethod authMethod) {
            require(authMethod, notNullValue());
            mAuthMethod = authMethod;
            return this;
        }

        /**
         * Specifies the display name for the hint. Empty values are treated as null.
         */
        public Builder setDisplayName(@Nullable String displayName) {
            mDisplayName = nullifyEmptyString(displayName);
            return this;
        }

        /**
         * Specifies the display picture URI for the hint, in string form. Empty values are
         * treated as null. Non-empty values must be valid http(s) URIs.
         */
        public Builder setDisplayPictureUri(@Nullable String displayPictureUri) {
            setDisplayPictureUri(nullifyEmptyString(displayPictureUri) != null
                            ? Uri.parse(displayPictureUri)
                            : null);
            return this;
        }

        /**
         * Specifies the display picture URI for the hint, in string form. Non-null values
         * must be valid http(s) URIs.
         */
        public Builder setDisplayPictureUri(@Nullable Uri displayPictureUri) {
            require(displayPictureUri, nullOr(isWebUri()));
            mDisplayPictureUri = displayPictureUri;
            return this;
        }

        /**
         * Specifies the generated password for the hint. Empty values are treated as null.
         */
        public Builder setGeneratedPassword(@Nullable String generatedPassword) {
            mGeneratedPassword = nullifyEmptyString(generatedPassword);
            return this;
        }

        /**
         * Specifies the "proof of access" ID token for the hint. Empty values are treated as null.
         */
        public Builder setIdToken(@Nullable String idToken) {
            mIdToken = nullifyEmptyString(idToken);
            return this;
        }

        /**
         * Specifies any additional, non-standard properties associated with the hint.
         */
        public Builder setAdditionalProperties(@Nullable Map<String, byte[]> additionalProps) {
            mAdditionalProps =
                    AdditionalPropertiesUtil.validateAdditionalProperties(additionalProps);
            return this;
        }

        private Builder setAdditionalPropertiesFromProto(
                @Nullable Map<String, ByteString> additionalProps) {
            mAdditionalProps =
                    AdditionalPropertiesUtil.validateAdditionalPropertiesFromProto(additionalProps);
            return this;
        }

        /**
         * Creates the {@link Hint hint} instance with the specified properties.
         */
        public Hint build() {
            return new Hint(this);
        }
    }

    private static final class HintCreator implements Creator<Hint> {

        @Override
        public Hint createFromParcel(Parcel source) {
            int length = source.readInt();
            byte[] hintBytes = new byte[length];
            source.readByteArray(hintBytes);
            try {
                return Hint.fromProtobufBytes(hintBytes);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to parse hint from parcel");
            }
        }

        @Override
        public Hint[] newArray(int size) {
            return new Hint[size];
        }
    }
}

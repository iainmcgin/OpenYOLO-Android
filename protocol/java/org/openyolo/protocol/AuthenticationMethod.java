/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
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

import static org.valid4j.Assertive.require;

import android.support.annotation.NonNull;
import java.io.IOException;
import org.openyolo.protocol.internal.CustomMatchers;


/**
 * A URI-based representation of an authentication method, which describes how a credential is
 * used to authenticate a user.
 */
public final class AuthenticationMethod implements Comparable<AuthenticationMethod> {

    /**
     * Creates an authentication method from its protocol buffer equivalent, in byte form.
     * @throws IOException if the protocol buffer cannot be parsed.
     */
    public static AuthenticationMethod fromProtobufBytes(byte[] protoBufBytes) throws IOException {
        return fromProtobuf(Protobufs.AuthenticationMethod.parseFrom(protoBufBytes));
    }

    /**
     * Creates an authentication method from its protocol buffer equivalent.
     */
    public static AuthenticationMethod fromProtobuf(Protobufs.AuthenticationMethod authMethod) {
        return new AuthenticationMethod(authMethod.getUri());
    }

    private final String mUri;

    /**
     * Creates an authentication method from a URI string.
     */
    public AuthenticationMethod(@NonNull String authMethodUriStr) {
        mUri = require(authMethodUriStr, CustomMatchers.isValidAuthenticationMethod());
    }

    /**
     * Creates a protocol buffer representation of the authentication method, for transmission or
     * storage.
     */
    public Protobufs.AuthenticationMethod toProtobuf() {
        return Protobufs.AuthenticationMethod.newBuilder()
                .setUri(mUri)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AuthenticationMethod)) {
            return false;
        }

        return compareTo((AuthenticationMethod)obj) == 0;
    }

    @Override
    public int hashCode() {
        return mUri.hashCode();
    }

    @Override
    public int compareTo(@NonNull AuthenticationMethod authMethod) {
        return mUri.compareTo(authMethod.mUri);
    }

    @Override
    public String toString() {
        return mUri;
    }
}

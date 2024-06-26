/*
 * Copyright (c) 2024 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.core.internal.util;

import androidx.annotation.NonNull;

import org.eclipse.uprotocol.common.UStatusException;
import org.eclipse.uprotocol.transport.builder.UAttributesBuilder;
import org.eclipse.uprotocol.transport.validate.UAttributesValidator;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPayload;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationResult;

import java.util.Optional;

public interface UMessageUtils {

    static @NonNull UMessage checkMessageValid(@NonNull UMessage message) {
        final UAttributes attributes = message.getAttributes();
        final ValidationResult result = UAttributesValidator.getValidator(attributes).validate(attributes);
        if (result.isFailure()) {
            throw new UStatusException(result.toStatus());
        }
        return message;
    }

    static @NonNull UMessage replaceSource(@NonNull UMessage message, @NonNull UUri source) {
        return UMessage.newBuilder(message)
                .setAttributes(UAttributes.newBuilder(message.getAttributes())
                        .setSource(source)
                        .build())
                .build();
    }

    static @NonNull UMessage replaceSink(@NonNull UMessage message, UUri sink) {
        if (sink == null || UriValidator.isEmpty(sink)) {
            return removeSink(message);
        } else {
            return UMessage.newBuilder(message)
                    .setAttributes(UAttributes.newBuilder(message.getAttributes())
                            .setSink(sink)
                            .build())
                    .build();
        }
    }

    static @NonNull UMessage removeSink(@NonNull UMessage message) {
        return UMessage.newBuilder(message)
                .setAttributes(UAttributes.newBuilder(message.getAttributes())
                        .clearSink()
                        .build())
                .build();
    }

    static @NonNull UMessage addSinkIfEmpty(@NonNull UMessage message, @NonNull UUri sink) {
        return UriValidator.isEmpty(message.getAttributes().getSink()) ? replaceSink(message, sink) : message;
    }

    static @NonNull UMessage buildResponseMessage(@NonNull UMessage requestMessage, @NonNull UPayload responsePayload) {
        return UMessage.newBuilder()
                .setAttributes(UAttributesBuilder.response(requestMessage.getAttributes()).build())
                .setPayload(responsePayload)
                .build();
    }

    /** NOTE: To be used only by dispatchers */
    static @NonNull UMessage buildFailedResponseMessage(@NonNull UMessage requestMessage, @NonNull UCode code) {
        return UMessage.newBuilder()
                .setAttributes(UAttributesBuilder.response(requestMessage.getAttributes()).withCommStatus(code).build())
                .build();
    }
}

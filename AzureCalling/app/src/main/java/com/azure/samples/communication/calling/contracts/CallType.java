// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.contracts;

import com.azure.android.core.util.ExpandableStringEnum;

public class CallType extends ExpandableStringEnum<CallType> {

    public static final CallType GROUP_CALL = fromString("GROUP_CALL");
    public static final CallType TEAMS_MEETING = fromString("TEAMS_MEETING");

    private static CallType fromString(final String name) {
        return fromString(name, CallType.class);
    }
}

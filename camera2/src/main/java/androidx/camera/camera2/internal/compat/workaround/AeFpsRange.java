/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.camera.camera2.internal.compat.workaround;

import android.hardware.camera2.CaptureRequest;
import android.util.Range;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.camera2.impl.Camera2ImplConfig;
import androidx.camera.camera2.internal.compat.quirk.AeFpsRangeLegacyQuirk;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.impl.Config;
import androidx.camera.core.impl.Quirks;

/**
 * Sets an AE target FPS range on legacy devices to maintain good exposure.
 *
 * @see AeFpsRangeLegacyQuirk
 */
public class AeFpsRange {

    @Nullable
    private final Range<Integer> mAeTargetFpsRange;

    /** Chooses the AE target FPS range on legacy devices. */
    public AeFpsRange(@NonNull final Quirks quirks) {
        final AeFpsRangeLegacyQuirk quirk = quirks.get(AeFpsRangeLegacyQuirk.class);
        if (quirk == null) {
            mAeTargetFpsRange = null;
        } else {
            mAeTargetFpsRange = quirk.getRange();
        }
    }

    /**
     * Sets the {@link android.hardware.camera2.CaptureRequest#CONTROL_AE_TARGET_FPS_RANGE}
     * option on legacy device when possible.
     */
    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    public void addAeFpsRangeOptions(@NonNull Camera2ImplConfig.Builder configBuilder) {
        if (mAeTargetFpsRange != null) {
            configBuilder.setCaptureRequestOptionWithPriority(
                    CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mAeTargetFpsRange,
                    Config.OptionPriority.REQUIRED);
        }
    }
}

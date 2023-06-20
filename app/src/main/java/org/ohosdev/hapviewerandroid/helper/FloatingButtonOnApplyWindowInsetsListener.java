/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ohosdev.hapviewerandroid.helper;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FloatingButtonOnApplyWindowInsetsListener implements View.OnApplyWindowInsetsListener {

    @NonNull
    private final Rect mPadding = new Rect();

    public FloatingButtonOnApplyWindowInsetsListener(@Nullable View view) {
        if (view != null) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            mPadding.set(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin,
                    layoutParams.bottomMargin);
        }
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets insets) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(mPadding.left, mPadding.top,
                mPadding.right + insets.getSystemWindowInsetRight(),
                mPadding.bottom + insets.getSystemWindowInsetBottom());
        view.setLayoutParams(layoutParams);
        return insets;
    }
}

/*
 * Copyright 2016 The Closure Rules Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

final class JsCheckerErrorManager extends BasicErrorManager {

  private final JsCheckerState state;
  private final MessageFormatter formatter;
  private int summaryDetailLevel = 1;

  JsCheckerErrorManager(JsCheckerState state, MessageFormatter formatter) {
    this.state = state;
    this.formatter = formatter;
  }

  @Override
  public void println(CheckLevel level, JSError error) {
    state.stderr.add(error.format(level, formatter));
  }

  public void setSummaryDetailLevel(int summaryDetailLevel) {
    this.summaryDetailLevel = summaryDetailLevel;
  }

  @Override
  public void printSummary() {
    if (summaryDetailLevel >= 3 ||
        (summaryDetailLevel >= 1 && getErrorCount() + getWarningCount() > 0) ||
        (summaryDetailLevel >= 2 && getTypedPercent() > 0.0)) {
      if (getTypedPercent() > 0.0) {
        state.stderr.add(
            String.format("%d error(s), %d warning(s), %.1f%% typed%n",
                getErrorCount(), getWarningCount(), getTypedPercent()));
      } else {
        state.stderr.add(
            String.format("%d error(s), %d warning(s)%n", getErrorCount(), getWarningCount()));
      }
    }
  }
}

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

import com.google.debugging.sourcemap.proto.Mapping.OriginalMapping;
import com.google.javascript.jscomp.SourceExcerptProvider.ExcerptFormatter;
import com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt;
import com.google.javascript.rhino.TokenUtil;

final class JsCheckerErrorFormatter extends AbstractMessageFormatter {

  private static final SourceExcerpt excerpt = SourceExcerptProvider.SourceExcerpt.LINE;
  private static final ExcerptFormatter excerptFormatter =
      new LightweightMessageFormatter.LineNumberingFormatter();

  private final JsCheckerState state;

  JsCheckerErrorFormatter(JsCheckerState state, SourceExcerptProvider source) {
    super(source);
    this.state = state;
  }

  @Override
  public String formatError(JSError error) {
    return format(error, false);
  }

  @Override
  public String formatWarning(JSError warning) {
    return format(warning, true);
  }

  private String format(JSError error, boolean warning) {
    SourceExcerptProvider source = getSource();
    String sourceName = error.sourceName;
    int lineNumber = error.lineNumber;
    int charno = error.getCharno();

    // Format the non-reverse-mapped position.
    StringBuilder b = new StringBuilder();
    StringBuilder boldLine = new StringBuilder();
    String nonMappedPosition = formatPosition(sourceName, lineNumber);

    // Check if we can reverse-map the source.
    OriginalMapping mapping = source == null ? null : source.getSourceMapping(
        error.sourceName, error.lineNumber, error.getCharno());
    if (mapping == null) {
      boldLine.append(nonMappedPosition);
    } else {
      sourceName = mapping.getOriginalFile();
      lineNumber = mapping.getLineNumber();
      charno = mapping.getColumnPosition();

      b.append(nonMappedPosition);
      b.append("\nOriginally at:\n");
      boldLine.append(formatPosition(sourceName, lineNumber));
    }

    // extract source excerpt
    String sourceExcerpt = source == null ? null :
        excerpt.get(
            source, sourceName, lineNumber, excerptFormatter);

    boldLine.append(getLevelName(warning ? CheckLevel.WARNING : CheckLevel.ERROR));
    String typeName = error.getType().key;
    String groupName = state.diagnosticGroups.get(error.getType());
    if (groupName != null) {
      boldLine.append(" ");
      boldLine.append(groupName);
      boldLine.append(" ");
      boldLine.append(typeName);
    }
    boldLine.append(" - ");
    boldLine.append(error.description);

    b.append(maybeEmbolden(boldLine.toString()));
    b.append('\n');
    if (sourceExcerpt != null) {
      b.append(sourceExcerpt);
      b.append('\n');

      // padding equal to the excerpt and arrow at the end
      // charno == sourceExpert.length() means something is missing
      // at the end of the line
      if (0 <= charno && charno <= sourceExcerpt.length()) {
        for (int i = 0; i < charno; i++) {
          char c = sourceExcerpt.charAt(i);
          if (TokenUtil.isWhitespace(c)) {
            b.append(c);
          } else {
            b.append(' ');
          }
        }
        b.append("^\n");
      }
    }
    return b.toString();
  }

  private static String formatPosition(String sourceName, int lineNumber) {
    StringBuilder b = new StringBuilder();
    if (sourceName != null) {
      b.append(sourceName);
      if (lineNumber > 0) {
        b.append(':');
        b.append(lineNumber);
      }
      b.append(": ");
    }
    return b.toString();
  }
}

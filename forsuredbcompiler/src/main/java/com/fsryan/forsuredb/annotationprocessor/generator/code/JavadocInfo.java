package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class JavadocInfo {

    public static final String AUTHOR_STRING = "@author <a href=\"https://github.com/ryansgot/forsuredbcompiler\">forsuredbcompiler</a>";
    private final String unformattedDoc;
    private final Object[] replacements;

    private JavadocInfo(List<String> lines, Object... replacements) {
        unformattedDoc = Joiner.on("\n").join(lines);
        this.replacements = replacements;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String stringToFormat() {
        return unformattedDoc;
    }

    public Object[] replacements() {
        return replacements;
    }

    public static String inlineClassLink(Class<?> className) {
        return className == null ? "" : "{@link " + className.getName().replaceAll("\\$", ".") + "}";
    }

    public static class Builder {

        private int indentionSize = 0;
        private String indention = "";

        private final List<String> lines = new ArrayList<>();
        private final List<Object> replacements = new ArrayList<>();

        private Builder() {}

        public Builder indent() {
            indentionSize += 1;
            indention += "  ";
            return this;
        }

        public Builder indentJava() {
            return indent().indent();
        }

        public Builder unindent() {
            indentionSize = indentionSize == 0 ? 0 : indentionSize - 1;
            indention = indentionSize == 0 ? "" : Strings.repeat("  ", indentionSize);
            return this;
        }

        public Builder unindentJava() {
            return unindent().unindent();
        }

        public Builder startParagraph() {
            return addLine("<p>").indent();
        }

        public Builder endParagraph() {
            return unindent().addLine("</p>");
        }

        public Builder startCode() {
            return addLine("<pre>").indent().addLine("{@code").indent().indent().indent().indent();
        }

        public Builder endCode() {
            return unindent().unindent().unindent().unindent().addLine("}").unindent().addLine("</pre>");
        }

        public Builder addLine() {
            return addLine("");
        }

        public Builder param(String paramName, String paramExplanation) {
            if (Strings.isNullOrEmpty(paramName)) {
                return this;
            }
            if (Strings.isNullOrEmpty(paramExplanation)) {
                return addLine("@param $L", paramName);
            }
            return addLine("@param $L $L", paramName, paramExplanation);
        }

        public Builder param(String paramName) {
            return param(paramName, null);
        }

        public Builder returns(String explanation, Object... replacements) {
            if (Strings.isNullOrEmpty(explanation)) {
                return addLine("@return");
            }
            return addLine("@return " + explanation, replacements);
        }

        public Builder addLine(String stringToFormat, Object... replacements) {
            lines.add(prependIndention(stringToFormat));
            for (Object replacement : replacements) {
                this.replacements.add(replacement);
            }
            return this;
        }

        public JavadocInfo build() {
            return new JavadocInfo(lines, replacements.toArray(new Object[replacements.size()]));
        }

        private String prependIndention(String lineToIndent) {
            return indention + lineToIndent;
        }
    }
}

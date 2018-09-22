package com.fsryan.forsuredb.util;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Locale;
import java.util.Map;

@AutoValue
public abstract class TestProcessingEnvironment implements ProcessingEnvironment {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setOptions(@Nullable Map<String, String> options);
        public abstract Builder setMessager(@Nullable Messager messager);
        public abstract Builder setFiler(@Nullable Filer filer);
        public abstract Builder setElementUtils(@Nullable Elements elementUtils);
        public abstract Builder setTypeUtils(@Nullable Types typeUtils);
        public abstract Builder setSourceVersion(@Nullable SourceVersion sourceVersion);
        public abstract Builder setLocale(@Nullable Locale locale);
        public abstract TestProcessingEnvironment build();
    }

    public static TestProcessingEnvironment withElements(Elements elements) {
        return builder().setElementUtils(elements).build();
    }

    public static Builder builder() {
        return new AutoValue_TestProcessingEnvironment.Builder();
    }

    @Nullable public abstract Map<String, String> getOptions();
    @Nullable public abstract Messager getMessager();
    @Nullable public abstract Filer getFiler();
    @Nullable public abstract Elements getElementUtils();
    @Nullable public abstract Types getTypeUtils();
    @Nullable public abstract SourceVersion getSourceVersion();
    @Nullable public abstract Locale getLocale();
    public abstract Builder toBuilder();
}

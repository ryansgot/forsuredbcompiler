/*
   forsuredbcompiler, an annotation processor and code generator for the forsuredb project

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.forsuredb.annotationprocessor.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.util.HashMap;
import java.util.Map;

public class AnnotationTranslatorFactory {

    private static AnnotationTranslatorFactory instance;

    private ProcessingEnvironment processingEnv;

    private AnnotationTranslatorFactory(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public static AnnotationTranslatorFactory init(ProcessingEnvironment processingEnv) {
        if (instance == null) {
            instance = new AnnotationTranslatorFactory(processingEnv);
        }
        return instance;
    }

    public static AnnotationTranslatorFactory inst() {
        if (instance == null) {
            throw new IllegalStateException("Must call init() prior to calling inst()");
        }
        return instance;
    }

    public AnnotationTranslator create(AnnotationMirror am) {
        return new AnnotationTranslator(processingEnv.getElementUtils().getElementValuesWithDefaults(am));
    }

    /**
     * <p>
     *     Translates from all elements of an {@link AnnotationMirror AnnotationMirror} to properties
     *     that can be referenced by their String names. If you pass in {@link AnnotationMirror#getElementValues()},
     *     then only the non-default values will be translated.
     * </p>
     * @author Ryan Scott
     * @see AnnotationTranslatorFactory
     */
    public static class AnnotationTranslator {

        private final Map<String, Object> annotation = new HashMap<>();

        /*package*/ AnnotationTranslator(Map<? extends ExecutableElement, ? extends AnnotationValue> elementToValueMap) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementToValueMap.entrySet()) {
                ExecutableElement ee = entry.getKey();
                AnnotationValue av = entry.getValue();
                this.annotation.put(ee.getSimpleName().toString(), av.getValue());
            }
        }

        /**
         * @param property essentially invokes the method of the annotation
         * @return A {@link Caster Caster} that can give you the uncasted value or the value cast to the
         * Type you wish
         */
        public Caster property(String property) {
            return new Caster(annotation.get(property));
        }
    }
}

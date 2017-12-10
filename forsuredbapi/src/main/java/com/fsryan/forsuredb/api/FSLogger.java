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
package com.fsryan.forsuredb.api;

/**
 * <p>
 *     Interface for loggers used by the forsuredb
 * </p>
 * @author Ryan Scott
 */
public interface FSLogger {

    SilentLog SILENT_LOG = new SilentLog();

    void e(String message, Object... replacements);
    void i(String message, Object... replacements);
    void w(String message, Object... replacements);
    void o(String message, Object... replacements);

    /**
     * <p>
     *     Does nothing.
     * </p>
     */
    class SilentLog implements FSLogger {
        public void e(String message, Object... replacements) {}
        public void i(String message, Object... replacements) {}
        public void w(String message, Object... replacements) {}
        public void o(String message, Object... replacements) {}
    }

    /**
     * <p>
     *     Prints log messages to {@link System#out System.out}
     * </p>
     */
    class DefaultFSLogger implements FSLogger {

        private static final String TAG = "FS_DEFAULT_LOGGER";

        @Override
        public void e(String message, Object... replacements) {
            message = String.format(message, replacements);
            System.out.println(String.format("%s/[%s]: %s", "E", TAG, message));
        }

        @Override
        public void i(String message, Object... replacements) {
            message = String.format(message, replacements);
            System.out.println(String.format("%s/[%s]: %s", "I", TAG, message));
        }

        @Override
        public void w(String message, Object... replacements) {
            message = String.format(message, replacements);
            System.out.println(String.format("%s/[%s]: %s", "W", TAG, message));

        }

        @Override
        public void o(String message, Object... replacements) {
            message = String.format(message, replacements);
            System.out.println(String.format("%s/[%s]: %s", "O", TAG, message));
        }
    }
}

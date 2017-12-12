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

import java.io.Closeable;

/**
 * <p>
 *     An interface capable of pulling information from a database query
 * </p>
 * @author Ryan Scott
 */
public interface Retriever extends Closeable {
    String getString(String column);
    int getInt(String column);
    long getLong(String column);
    double getDouble(String column);
    float getFloat(String column);
    byte[] getBytes(String column);
    int getCount();

    boolean isClosed();
    void close();

    // navigation methods
    boolean moveToPrevious();
    boolean moveToFirst();
    boolean moveToNext();
    boolean moveToPosition(int position);
    boolean move(int offset);
    boolean moveToLast();
    boolean isAfterLast();
    boolean isBeforeFirst();
    boolean isFirst();
    boolean isLast();
    int getPosition();
}

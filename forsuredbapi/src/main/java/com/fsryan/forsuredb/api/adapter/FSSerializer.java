package com.fsryan.forsuredb.api.adapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

public interface FSSerializer {
    /**
     * <p>
     *
     * </p>
     * @return true if your serializer serializes best to a byte array. Otherwise, return false
     */
    boolean storeAsBlob();
    String createStringDoc(Type type, Object val);
    byte[] createBlobDoc(Type type, Object val);
    <T> T fromStorage(Type typeOfT, byte[] objectBytes);
    <T> T fromStorage(Type typeOfT, String stringRepresentation);

    /**
     * <p>
     *     The default serializer. You should never rely upon this, but it may be able to
     *     serialize your java objects properly
     * </p>
     */
    FSSerializer JAVA_SERIALIZABLE_SERIALIZER = new FSSerializer() {
        @Override
        public boolean storeAsBlob() {
            return true;
        }

        @Override
        public String createStringDoc(Type type, Object val) {
            return new String(createBlobDoc(type, val));
        }

        @Override
        public byte[] createBlobDoc(Type type, Object val) {
            ObjectOutputStream out = null;
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] outBytes = null;
            try {
                out = new ObjectOutputStream(byteStream);
                out.writeObject(outBytes);
                outBytes = byteStream.toByteArray();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ioe2) {}
                }
                try {
                    byteStream.close();
                } catch (IOException ioe2) {}
            }
            return outBytes == null ? new byte[0] : outBytes;
        }
    }
}

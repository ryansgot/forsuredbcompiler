package com.fsryan.forsuredb.api.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

public class FSDefaultSerializer extends FSByteArraySerializer {

    @Override
    public byte[] createBlobDoc(Type type, Object val) {
        ObjectOutputStream objectOut = null;
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try {
            objectOut = new ObjectOutputStream(bytesOut);
            objectOut.writeObject(val);
            return bytesOut.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException ioe2) {}
            }
        }
    }

    @Override
    public <T> T fromStorage(Type typeOfT, byte[] objectBytes) {
        ObjectInputStream objectIn = null;
        try {
            objectIn = new ObjectInputStream(new ByteArrayInputStream(objectBytes));
            return (T) objectIn.readObject();
        } catch (Exception e) {
           throw new RuntimeException(e);
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException ioe) {}
            }
        }
    }
}

package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.api.FSDocStoreSaveApi;
import com.fsryan.forsuredb.api.FSSaveApi;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

@RunWith(Parameterized.class)
public class IntefaceHelperTest {

    private final Class<?> intf;
    private final Class<?>[] interfaces;

    public IntefaceHelperTest(Class<?> intf, List<Class<?>> interfaces) {
        this.intf = intf;
        this.interfaces = interfaces.toArray(new Class<?>[0]);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {FSGetApiExtensionTestTableSetter.class, Lists.newArrayList(FSGetApiExtensionTestTableSetter.class, FSSaveApi.class)},
                {FSDocStoreGetApiExtensionTestTableSetter.class, Lists.newArrayList(FSDocStoreGetApiExtensionTestTableSetter.class, FSDocStoreSaveApi.class, FSSaveApi.class)},
                {ColumnInfo.class, Lists.newArrayList(Comparable.class)}
        });
    }

    @Test
    public void shouldGetCorrectInterfaces() {
        assertArrayEquals(interfaces, InterfaceHelper.getInterfaces(intf));
    }
}

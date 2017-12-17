package com.fsryan.forsuredb;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

public abstract class AbstractDBOpenerTest {

    private static final String jdbcUrl = "jdbc:sqlite::memory:";

    @Mock
    protected DBConfigurer mockConfigurer;

    protected AbstractDBOpener openerUnderTest;

    protected final int currentVersion;
    protected final int newVersion;


    protected List<String> lifecycleMethodCalls;

    AbstractDBOpenerTest() {
        this(0, 1);
    }

    AbstractDBOpenerTest(int currentVersion, int newVersion) {
        this.currentVersion = currentVersion;
        this.newVersion = newVersion;
    }

    @Before
    public void initOpenerUnderTest() throws SQLException {
        MockitoAnnotations.initMocks(this);

        when(mockConfigurer.discoverVersion(any(Connection.class))).thenReturn(currentVersion);

        lifecycleMethodCalls = new ArrayList<>();

        openerUnderTest = new AbstractDBOpener(jdbcUrl, null, mockConfigurer, newVersion) {

            @Override
            public void onConfigure(Connection db) throws SQLException {
                lifecycleMethodCalls.add("onConfigure");
            }

            @Override
            public void onCreate(Connection db) throws SQLException {
                lifecycleMethodCalls.add("onCreate");
            }

            @Override
            public void onDowngrade(Connection db, int version, int newVersion) throws SQLException {
                lifecycleMethodCalls.add("onDowngrade");
            }

            @Override
            public void onUpgrade(Connection db, int version, int newVersion) throws SQLException {
                lifecycleMethodCalls.add("onUpgrade");
            }

            @Override
            public void onOpen(Connection db) throws SQLException {
                lifecycleMethodCalls.add("onOpen");
            }
        };
    }

    @After
    public void releaseResources() throws NoSuchFieldException, IllegalAccessException, SQLException {
        Connection db = accessDb(openerUnderTest);
        if (db != null) {
            db.close();
        }
    }

    public static class BasicFlow extends AbstractDBOpenerTest {
        @Test
        public void shouldNotBeInitializingAfterGetDatabase() throws Exception {
            openerUnderTest.getDatabaseLocked(false);
            assertFalse(accessIsInitializing(openerUnderTest));
        }

        @Test
        public void shouldCorrectlyOrderCallbacks() throws SQLException {
            openerUnderTest.getDatabaseLocked(false);
            assertEquals(Arrays.asList("onConfigure", "onCreate", "onOpen"), lifecycleMethodCalls);
            verify(mockConfigurer).setVersion(any(Connection.class), eq(newVersion));
        }
    }

    public static class UpgradeCallbackOrderTest extends AbstractDBOpenerTest {

        public UpgradeCallbackOrderTest() {
            super(1, 2);
        }

        @Test
        public void shouldCorrectlyOrderCallbacks() throws SQLException {
            openerUnderTest.getDatabaseLocked(false);
            assertEquals(Arrays.asList("onConfigure", "onUpgrade", "onOpen"), lifecycleMethodCalls);
            verify(mockConfigurer).setVersion(any(Connection.class), eq(newVersion));
        }
    }

    public static class DowngradeCallbackOrderTest extends AbstractDBOpenerTest {

        public DowngradeCallbackOrderTest() {
            super(2, 1);
        }

        @Test
        public void shouldCorrectlyOrderCallbacks() throws SQLException {
            openerUnderTest.getDatabaseLocked(false);
            assertEquals(Arrays.asList("onConfigure", "onDowngrade", "onOpen"), lifecycleMethodCalls);
            verify(mockConfigurer).setVersion(any(Connection.class), eq(newVersion));
        }
    }

    static abstract class ExistingDbTest extends AbstractDBOpenerTest {

        protected Connection mockConnection;

        private boolean isClosed;
        private boolean currentlyReadOnly;

        ExistingDbTest(boolean isClosed, boolean currentlyReadOnly) {
            this.isClosed = isClosed;
            this.currentlyReadOnly = currentlyReadOnly;
        }


        @Before
        public void setUpCurrentConnection() throws Exception {
            mockConnection = mock(Connection.class);
            when(mockConnection.isClosed()).thenReturn(isClosed);
            when(mockConnection.isReadOnly()).thenReturn(currentlyReadOnly);

            setDb(openerUnderTest, mockConnection);
        }
    }

    @RunWith(Parameterized.class)
    public static class ReturnEarlyFlow extends ExistingDbTest {

        private boolean requestWritable;

        public ReturnEarlyFlow(boolean currentlyReadOnly, boolean requestWritable) {
            super(false, currentlyReadOnly);
            this.requestWritable = requestWritable;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: should return existing connection when not requesting writable database and currently writable
                            false,          // currentlyReadOnly
                            false,          // requestWritable
                    },
                    {   // 01: should return existing connection when not requesting writable database and currently readable
                            true,           // currentlyReadOnly
                            false,          // requestWritable
                    },
                    {   // 02: should return existing connection when not requesting writable database and currently writable
                            false,          // currentlyReadOnly
                            true,           // requestWritable
                    }
            });
        }

        @Test
        public void shouldNotReconnectWhenCurrentConne() throws Exception {
            assertEquals(mockConnection, openerUnderTest.getDatabaseLocked(requestWritable));
        }
    }

    public static class ChangeReadableToWritableFlow extends ExistingDbTest {

        private Connection actual;

        public ChangeReadableToWritableFlow() {
            super(false, true);
        }

        @Before
        public void setUpMockConnection() throws SQLException {
            // makes mock work like a real value for setting readOnly
            doAnswer(invocation -> {
                when(mockConnection.isReadOnly()).thenReturn(invocation.getArgument(0));
                return null;
            }).when(mockConnection).setReadOnly(anyBoolean());
            actual = openerUnderTest.getDatabaseLocked(true);
        }

        @Test
        public void shouldReturnExistingConnection() throws SQLException {
            assertEquals(mockConnection, actual);
        }

        @Test
        public void shouldSetReadableFalse() throws SQLException {
            verify(mockConnection).setReadOnly(eq(false));
        }
    }

    @RunWith(Parameterized.class)
    public static class AlreadyClosedFlow extends ExistingDbTest {

        public AlreadyClosedFlow(boolean currentlyReadOnly) {
            super(true, currentlyReadOnly);
        }

        @Parameterized.Parameters
        public static Iterable<Object> data() {
            return Arrays.asList(false, true);
        }

        @Test
        public void shouldNotReturnClosedConnection() throws SQLException {
            assertNotEquals(mockConnection, openerUnderTest.getDatabaseLocked(false));
        }
    }

    static boolean accessIsInitializing(AbstractDBOpener opener) throws IllegalAccessException, NoSuchFieldException {
        Field initializingField = AbstractDBOpener.class.getDeclaredField("isInitializing");
        initializingField.setAccessible(true);
        return (boolean) initializingField.get(opener);
    }

    static Connection accessDb(AbstractDBOpener opener) throws NoSuchFieldException, IllegalAccessException, SQLException {
        return (Connection) dbField().get(opener);
    }

    static void setDb(AbstractDBOpener opener, Connection db) throws NoSuchFieldException, IllegalAccessException {
        dbField().set(opener, db);
    }

    private static Field dbField() throws NoSuchFieldException {
        Field dbField = AbstractDBOpener.class.getDeclaredField("db");
        dbField.setAccessible(true);
        return dbField;
    }
}

package com.fsryan.forsuredb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionUtilTest {

    @Mock
    private Connection mockConnection;

    @Test
    public void shouldNotInteractWithConnectionAgainIfNotAutocommit() throws SQLException {
        when(mockConnection.getAutoCommit()).thenReturn(false);

        assertFalse(ConnectionUtil.ensureNotAutoCommit(mockConnection));

        verify(mockConnection).getAutoCommit();
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    public void shouldSetAutocommitFalseWhenCurrentlyAutocommit() throws SQLException {
        when(mockConnection.getAutoCommit()).thenReturn(true);

        assertTrue(ConnectionUtil.ensureNotAutoCommit(mockConnection));

        InOrder inOrder = inOrder(mockConnection);
        inOrder.verify(mockConnection).getAutoCommit();
        inOrder.verify(mockConnection).setAutoCommit(eq(false));
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    public void shouldReturnFalseOnNullInput() throws SQLException {
        assertFalse(ConnectionUtil.ensureNotAutoCommit(null));
    }
}

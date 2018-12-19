package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.sqlite.SqliteMasterAssertions;
import com.fsryan.forsuredb.sqlite.StatementUtil;
import com.fsryan.forsuredb.sqlitelib.FreshDBForClassExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableFQClassName;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;

@ExtendWith(FreshDBForClassExtension.class)
public class AddColumnsGeneratorIntegrationTest {

    public static Iterable<Arguments> generatedSqlInput() {
        return AddColumnsGeneratorTest.shouldGenerateCorrectSqlInput();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("generatedSqlInput")
    @DisplayName("Should successfully add column to table with generated SQL")
    public void shouldCorrectlyGenerateSql(String desc, Set<ColumnInfo> columnsToAdd, List<String> _ignore, Connection conn) throws Exception {
        createBeginningSchema(conn, tableMapOf(tableBuilder("t1").build()));

        final String tableClassName = tableFQClassName("t1");
        final Map<String, TableInfo> schema = tableMapOf(
                tableBuilder("t1")
                        .addAllColumns(columnsToAdd)
                        .build()
        );
        Set<String> columnNames = columnsToAdd.stream().map(ColumnInfo::getColumnName).collect(Collectors.toSet());
        List<String> statements = new AddColumnsGenerator(tableClassName, schema, columnNames).statements();

        for (String sql : statements) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.execute();
            }
        }

        List<String> sqlAssertions = SqliteMasterAssertions.forAllTableInfoPlus(schema.values());
        SqliteMasterAssertions.makeAllAssertions(conn, sqlAssertions);
    }

    // TODO: this may not work properly due to foreign key constraint issues
    private static void createBeginningSchema(Connection conn, Map<String, TableInfo> schema) throws SQLException {
        for (String tableClassName : schema.keySet()) {
            StatementUtil.executeScript(conn, new CreateTableGenerator(tableClassName, schema).statements());
        }
    }
}

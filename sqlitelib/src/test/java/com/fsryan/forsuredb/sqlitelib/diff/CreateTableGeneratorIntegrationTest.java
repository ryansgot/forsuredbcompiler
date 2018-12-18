package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.sqlitelib.FreshDBForClassExtension;
import com.fsryan.forsuredb.sqlitelib.SqliteMasterAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

@ExtendWith(FreshDBForClassExtension.class)
public class CreateTableGeneratorIntegrationTest {

    public static Iterable<Arguments> generatedSqlInput() {
        return CreateTableGeneratorTest.shouldGenerateCorrectSqlInput();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("generatedSqlInput")
    @DisplayName("Should successfully create table with generated SQL")
    public void createTable(String desc, String tableClassName, Map<String, TableInfo> schema, List<String> _ignore, Connection conn) throws Exception {
        List<String> statements = new CreateTableGenerator(tableClassName, schema).statements();
        for (String sql : statements) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.execute();
            }
        }

        List<String> checkStatements = SqliteMasterAssertions.forTableInfo(schema.get(tableClassName));
        SqliteMasterAssertions.makeAllAssertions(conn, checkStatements);
    }
}

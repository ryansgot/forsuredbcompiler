package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;
import com.fsryan.forsuredb.sqlitelib.FreshDBForClassExtension;
import com.fsryan.forsuredb.sqlitelib.FreshDbForEachTestExtension;
import com.fsryan.forsuredb.sqlitelib.SqliteMasterAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(FreshDbForEachTestExtension.class)
public class RecreateTableGeneratorIntegrationTest {

    public static Iterable<Arguments> sqlApplicationInput() {
        return RecreateTableGeneratorTest.recreateTableInput();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("sqlApplicationInput")
    @DisplayName("should generate correct table recreation SQL")
    public void sqlGeneration(String desc, List<String> dbSetupSql, String tableClassName, Map<String, TableInfo> schema, SchemaDiff diff, List<String> _ignore, Connection conn) throws Exception {
        StatementUtil.executeScript(conn, dbSetupSql);

        List<String> generatedStatements = new RecreateTableGenerator(tableClassName, schema, diff).statements();
        StatementUtil.executeScript(conn, generatedStatements);

        List<String> assertions = SqliteMasterAssertions.forAllTableInfoPlus(schema.values());
        SqliteMasterAssertions.makeAllAssertions(conn, assertions);
    }
}

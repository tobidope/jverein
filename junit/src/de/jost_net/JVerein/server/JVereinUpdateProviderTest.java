package de.jost_net.JVerein.server;

import de.willuhn.util.ProgressMonitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JVereinUpdateProviderTest
{
  private static final MySQLContainer<?> container = new MySQLContainer<>(
      "mysql:8").withUrlParam("useUnicode", "Yes")
      .withUrlParam("characterEncoding", "ISO8859_1");
  @Mock
  private ProgressMonitor monitor;

  @BeforeAll
  static void beforeAll()
  {
    container.start();
  }

  @AfterAll
  static void afterAll()
  {
    container.close();
  }

  private static Stream<Arguments> provider() throws SQLException
  {
    Connection h2Connection = DriverManager.getConnection(
        "jdbc:h2:mem:testdb", "sa", "");

    var mysqlConnection = DriverManager.getConnection(container.getJdbcUrl(),
        container.getUsername(), container.getPassword());

    return Stream.of(
        Arguments.of(Named.named("H2", h2Connection), DBSupportH2Impl.class.getName()),
        Arguments.of(Named.named("mySQL", mysqlConnection), DBSupportMySqlImpl.class.getName()));
  }

  @DisplayName("Test migrations for H2 and MySQL")
  @ParameterizedTest
  @MethodSource("provider")
  void testMigrations(Connection connection, String driver)
  {
    assertDoesNotThrow(() -> {
      final var updateProvider = new JVereinUpdateProvider(connection, monitor,
          driver);
      assertEquals(463, updateProvider.getCurrentVersion(connection));
    });

    verify(monitor, atLeast(1)).setStatusText(Mockito.anyString());
  }
}

package db;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


public class EmbeddedPostgres {

  public static final String DATABASE_NAME = "users";
  public static final String USERNAME = "postgres";
  public static final String PASSWORD = "secret";

  public static int startPostgres() {
    var pg = new PostgreSQLContainer<> (DockerImageName.parse("postgres:13.3-alpine"))
      .withDatabaseName(DATABASE_NAME)
      .withUsername(USERNAME)
      .withPassword(PASSWORD)
      .withInitScript("setup.sql");
      pg.start();
    return pg.getFirstMappedPort();
  }
}

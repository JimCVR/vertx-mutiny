package db;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxMutinyWebReactiveSQL extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(VertxMutinyWebReactiveSQL.class);
  private static final int HTTP_SERVER_PORT = 9000;

  public static void main(String[] args) {
    var port = EmbeddedPostgres.startPostgres();
    final var options = new DeploymentOptions()
      .setConfig(new JsonObject().put("port", port));
    Vertx.vertx().deployVerticle(new VertxMutinyWebReactiveSQL(),options)
      .subscribe().with(id -> LOG.info("Started: {}", id));
  }


  @Override
  public Uni<Void> asyncStart() {
    var db = createPgPool(config());
    var router = Router.router(vertx);
    router.route().failureHandler(this::failureHandler);
    router.get("/users").respond(context -> executeQuery(db));


    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(HTTP_SERVER_PORT)
      .replaceWithVoid();
  }

  private Uni<JsonArray> executeQuery(Pool db) {
    LOG.info("Executing DB query to find all users...");
    return db.query("SELECT * FROM users")
      .execute()
      .onItem().transform(rows -> {
        var data = new JsonArray();
        for(Row row : rows){
          data.add(row.toJson());
        }
        LOG.info("Return data: {}", data);
        return data;
      }).onFailure().invoke(failure -> LOG.error("Failed query:", failure))
      .onFailure().recoverWithItem(new JsonArray());
  }

  private Pool createPgPool(JsonObject config) {
    var  connectOptions = new PgConnectOptions()
      .setHost("localhost")
      .setPort(config.getInteger("port"))
      .setDatabase(EmbeddedPostgres.DATABASE_NAME)
      .setUser(EmbeddedPostgres.USERNAME)
      .setPassword(EmbeddedPostgres.PASSWORD);

    var poolOptions = new PoolOptions().setMaxSize(5);
    return PgPool.pool(vertx, connectOptions, poolOptions);
  }

  private void failureHandler(RoutingContext failure) {
    failure.response().setStatusCode(500).endAndForget("Something went wrong :(");
  }

  private Uni<JsonArray> getUsers(RoutingContext routingContext) {
    final var responseBody = new JsonArray();
    responseBody.add(new JsonObject().put("name", "Alice"));
    responseBody.add(new JsonObject().put("name", "Leah"));
    responseBody.add(new JsonObject().put("name", "Melody"));

    return Uni.createFrom().item(responseBody);
  }
}

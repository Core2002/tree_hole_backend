package fun.fifu.server.tree_hole_backend;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {
  Vertx vertx = Vertx.vertx();

  @Test
  void savePojo(VertxTestContext testContext) {
    MongoClient client = MongoClient.createShared(vertx, new JsonObject()
      .put("host", "localhost")
      .put("port", 27017)
      .put("db_name", "tree_hole")
    );

    JsonObject document = new JsonObject().put("name", "Alan Turing");
    client.save("test", document).onComplete(handler -> {
      if (handler.succeeded()) {
        System.out.println(handler.result());
        testContext.completeNow();
      } else {
        testContext.failNow("F - " + handler.result());
      }
    });

  }

}

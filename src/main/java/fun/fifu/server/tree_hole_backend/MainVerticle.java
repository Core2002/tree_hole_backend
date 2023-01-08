package fun.fifu.server.tree_hole_backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fun.fifu.server.tree_hole_backend.pojo.HoleMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainVerticle extends AbstractVerticle {
  public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    MongoClient client = MongoClient.createShared(vertx, new JsonObject()
      .put("host", "localhost")
      .put("port", 27017)
      .put("db_name", "tree_hole")
    );

    router.get("/api/read_index/:hole/:index")
      .handler(ctx -> {
        ctx.response().putHeader("content-type", "text/json");
        String hole = ctx.pathParam("hole");
        int index = Integer.parseInt(ctx.pathParam("index"));

        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(Calendar.MONTH, -1);

        client.find("messages", new JsonObject(), res -> {
          if (res.succeeded() && index < res.result().size()) {
            List<JsonObject> result = res.result();
            ctx.response().end(gson.toJson(HoleMessage.fromJsonObject(result.get(index))));
          } else {
            ctx.response().end(gson.toJson(
              new HoleMessage()
                .hole("")
                .message("")
                .date(new Date())
                .like(0)
                .ip("")
            ));
          }
        });
      });

    router.get("/some/path")
      .respond(ctx -> {
        return Future.succeededFuture("nmsl");
      });

    server.requestHandler(router).listen(8080);
  }
}

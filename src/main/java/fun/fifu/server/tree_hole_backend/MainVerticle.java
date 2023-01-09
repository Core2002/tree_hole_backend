package fun.fifu.server.tree_hole_backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fun.fifu.server.tree_hole_backend.pojo.HoleMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainVerticle extends AbstractVerticle {
  SimpleDateFormat collectionNameFormat = new SimpleDateFormat("yyyy-MM");

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
        var query = new JsonObject().put("hole", hole);
        client.find(collectionNameFormat.format(new Date()), query, res -> {
          if (res.succeeded() && index < res.result().size()) {
            List<JsonObject> result = res.result();
            ctx.response().end(getPojoJson(getPojo(result.get(index))));
          } else {
            ctx.response().end(getPojoJson(
              new HoleMessage()
                .hole("")
                .message("")
                .date(System.currentTimeMillis())
                .like(0L)
                .ip("")));
          }
        });
      });

    server.requestHandler(router).listen(8080);
  }

  public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public HoleMessage getPojo(JsonObject jsonObject) {
    return gson.fromJson(jsonObject.toString(), HoleMessage.class);
  }

  public String getPojoJson(HoleMessage pojo) {
    return gson.toJson(pojo);
  }

  public JsonObject getPojoJsonObject(HoleMessage pojo) {
    return new JsonObject(getPojoJson(pojo));
  }

}

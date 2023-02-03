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
import org.bson.types.ObjectId;

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

    router.get("/api/read_index/:hole/:index").handler(ctx -> {
      ctx.response().putHeader("content-type", "text/json");
      String hole = ctx.pathParam("hole");
      int index = Integer.parseInt(ctx.pathParam("index"));
      var query = new JsonObject().put("hole", hole);
      client.find(collectionNameFormat.format(new Date()), query, res -> {
        if (res.succeeded() && index < res.result().size()) {
          List<JsonObject> result = res.result();
          HoleMessage pojo = getPojo(result.get(index));
          if (pojo.report() < 3) {
            ctx.response().end(getPojoJson(pojo));
            return;
          }
        }
        ctx.response().end(getPojoJson(
          new HoleMessage()
            ._id("blocked")
            .hole("")
            .message("")
            .date(System.currentTimeMillis())
            .like(0L)
            .ip("")));
      });
    });

    router.get("/api/read_hole_size/:hole").handler(ctx -> {
      ctx.response().putHeader("content-type", "text/json");
      String hole = ctx.pathParam("hole");
      client.count(collectionNameFormat.format(new Date()), new JsonObject().put("hole", hole), res -> {
        if (res.succeeded()) {
          ctx.response().end(
            new JsonObject()
              .put("size", res.result())
              .toString()
          );
        } else {
          ctx.response().end("{\"size\":-1}");
        }
      });
    });

    router.get("/api/add_hole_message/:hole/:message").handler(ctx -> {
      ctx.response().putHeader("content-type", "text/json");
      String hole = ctx.pathParam("hole");
      String message = ctx.pathParam("message");
      HoleMessage holeMessage = new HoleMessage()
        ._id(new ObjectId().toHexString())
        .hole(hole)
        .message(message)
        .like(0L)
        .date(System.currentTimeMillis())
        .ip(ctx.request().localAddress().hostAddress());
      client.save(collectionNameFormat.format(new Date()), getPojoJsonObject(holeMessage), res -> {
        if (res.succeeded()) {
          ctx.response().end("{\"res\":\"200 ok\"}");
        } else {
          ctx.response().end(
            new JsonObject()
              .put("res", res.result())
              .toString()
          );
        }
      });
    });

    router.get("/api/like_message/:message_id").handler(ctx -> {
      ctx.response().putHeader("content-type", "text/json");
      String message_id = ctx.pathParam("message_id");

      client.findOneAndUpdate(collectionNameFormat.format(new Date()),
        new JsonObject().put("_id", message_id),
        new JsonObject().put("$inc", new JsonObject().put("like", 1)),
        res -> {
          if (res.succeeded()) {
            ctx.response().end("{\"res\":\"200 ok\"}");
          } else {
            ctx.response().end(
              new JsonObject()
                .put("res", res.result())
                .toString()
            );
          }
        }
      );
    });

    router.get("/api/report_message/:message_id").handler(ctx -> {
      ctx.response().putHeader("content-type", "text/json");
      String message_id = ctx.pathParam("message_id");

      client.findOneAndUpdate(collectionNameFormat.format(new Date()),
        new JsonObject().put("_id", message_id),
        new JsonObject().put("$inc", new JsonObject().put("report", 1)),
        res -> {
          if (res.succeeded()) {
            ctx.response().end("{\"res\":\"200 ok\"}");
          } else {
            ctx.response().end(
              new JsonObject()
                .put("res", res.result())
                .toString()
            );
          }
        }
      );
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

package fun.fifu.server.tree_hole_backend.pojo;

import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@Accessors(fluent = true, chain = true)
public class HoleMessage {
  ObjectId id;
  String hole;
  String message;
  Date date;
  Integer like;
  String ip;

  public static HoleMessage fromJsonObject(JsonObject jo) {
    return new HoleMessage()
      .hole(jo.getString("hole"))
      .message(jo.getString("message"))
      .like(jo.getInteger("like"))
      .ip(jo.getString("ip"));
  }
}

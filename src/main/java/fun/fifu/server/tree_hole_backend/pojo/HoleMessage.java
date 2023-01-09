package fun.fifu.server.tree_hole_backend.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
public class HoleMessage {
  String _id;
  String hole;
  String message;
  Long date;
  Long like;
  String ip;
}

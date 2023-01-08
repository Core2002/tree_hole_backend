package fun.fifu.server.tree_hole_backend;

import io.vertx.core.Vertx;

public class Starter {
  public static void main(final String... args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }
}

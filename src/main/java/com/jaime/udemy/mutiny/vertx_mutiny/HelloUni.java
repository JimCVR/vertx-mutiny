package com.jaime.udemy.mutiny.vertx_mutiny;

import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloUni {

  private static final Logger LOG = LoggerFactory.getLogger(HelloUni.class);

  public static void main(String[] args) {
    Uni.createFrom().item("Hello")
      .onItem().transform(item -> item + " world!")
      .onItem().transform(String::toUpperCase)
      .subscribe().with(item -> LOG.info("item: {}" , item));

    Uni.createFrom().item("number")
      .onItem().castTo(Integer.class).onFailure().recoverWithItem(0)
      .subscribe().with(item -> LOG.info("Item: {}",item),
        error -> LOG.error("Failure to convert: ", error));
  }
}

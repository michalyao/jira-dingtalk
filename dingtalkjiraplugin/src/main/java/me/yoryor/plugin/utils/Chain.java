package me.yoryor.plugin.utils;

import io.vertx.core.Future;

import java.util.function.Function;

public class Chain {

  public static <R, A, B> Future<R> chain(A input, Function<A, Future<B>> operation1,
                                          Function<B, Future<R>> operation2) {
    Future<R> future = Future.future();
    operation1.apply(input).setHandler(ar -> {
      if (ar.failed()) {
        future.fail(ar.cause());
      } else {
        operation2.apply(ar.result()).setHandler(future.completer());
      }
    });
    return future;
  }

  public static <R, A, B, C> Future<R> chain(A input,
                                             Function<A, Future<B>> operation1,
                                             Function<B, Future<C>> operation2,
                                             Function<C, Future<R>> operation3) {
    Future<R> future = Future.future();

    operation1.apply(input).setHandler(ar -> {
      if (ar.failed()) {
        future.fail(ar.cause());
      } else {
        operation2.apply(ar.result()).setHandler(ar2 -> {
              if (ar2.failed()) {
                future.fail(ar2.cause());
              } else {
                operation3.apply(ar2.result()).setHandler(future.completer());
              }
            }
        );
      }
    });
    return future;
  }

  public static <R, A, B, C> Future<R> chain(Future<A> input,
                                             Function<A, Future<B>> operation1,
                                             Function<B, Future<C>> operation2,
                                             Function<C, Future<R>> operation3) {
    Future<R> future = Future.future();

    input.setHandler(arg -> {
      if (arg.failed()) {
        future.fail(arg.cause());
      } else {
        Future<R> chain = chain(arg.result(), operation1, operation2, operation3);
        chain.setHandler(future.completer());
      }
    });


    return future;
  }
  public static <R, A, B> Future<R> chain(Future<A> input,
                                          Function<A, Future<B>> operation1,
                                          Function<B, Future<R>> operation2) {
    Future<R> future = Future.future();

    input.setHandler(arg -> {
      if (arg.failed()) {
        future.fail(arg.cause());
      } else {
        Future<R> chain = chain(arg.result(), operation1, operation2);
        chain.setHandler(future.completer());
      }
    });


    return future;
  }

}

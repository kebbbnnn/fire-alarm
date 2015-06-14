package org.kebn.firealarm.handlers;

/**
 * Created by Kevin on 6/13/2015.
 */
public class AsyncTaskHandler<T> {
  private T         result;
  private Exception error;


  public T getResult() {
    return result;
  }

  public Exception getError() {
    return error;
  }


  public AsyncTaskHandler(T result) {
    super();
    this.result = result;
  }


  public AsyncTaskHandler(Exception error) {
    super();
    this.error = error;
  }
}

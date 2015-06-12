package org.kebn.firealarm.handlers;

import org.kebn.firealarm.utils.LogUtil;

import rx.functions.Action1;

/**
 * Created by Kevin on 6/12/2015.
 */
public class ErrorHandler implements Action1<Throwable> {
  @Override
  public void call(Throwable throwable) {
    LogUtil.e("Error", throwable);
  }
}

package org.kebn.firealarm.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import org.kebn.firealarm.R;

/**
 * Created by Kevin on 6/14/2015.
 */
public class StatefullButton extends Button implements View.OnClickListener {
  public enum State {
    FETCHING {
      @Override
      public String toString() {
        return "fetching";
      }
    }, READY {
      @Override
      public String toString() {
        return "ready";
      }
    }, FAILED {
      @Override
      public String toString() {
        return "failed";
      }
    };
  }

  private State           mState;
  private OnStateListener mOnStateListener;

  public StatefullButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public StatefullButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public StatefullButton(Context context) {
    super(context);
    init();
  }

  private void init() {
    setOnClickListener(this);
  }

  public void setOnStateListener(OnStateListener onStateListener) {
    mOnStateListener = onStateListener;
  }

  public void setState(State state) {
    mState = state;
    if (mState.toString().equals(State.FETCHING.toString())) {
      setText("Fetching Location...");
      setTextSize(20);
      setBackgroundDrawable(getResources().getDrawable(R.drawable.button_red_selector));
      setEnabled(false);
      mOnStateListener.onPreparing();
    } else if (mState.toString().equals(State.READY.toString())) {
      setText("Send Alarm!");
      setTextSize(24);
      setBackgroundDrawable(getResources().getDrawable(R.drawable.button_green_selector));
      setEnabled(true);
    } else if (mState.toString().equals(State.FAILED.toString())) {
      setText("Failed! Retry?");
      setTextSize(24);
      setBackgroundDrawable(getResources().getDrawable(R.drawable.button_red_selector));
      setEnabled(true);
    }
  }

  public State getState() {
    return mState;
  }

  @Override
  public void onClick(View view) {
    if (mState.toString().equals(State.FETCHING.toString())) {
      //do nothing
    } else if (mState.toString().equals(State.READY.toString())) {
      mOnStateListener.onSend();
    } else if (mState.toString().equals(State.FAILED.toString())) {
      mOnStateListener.onRetry();
    }
  }

  public interface OnStateListener {
    public void onPreparing();

    public void onSend();

    public void onRetry();
  }
}

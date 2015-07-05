package org.kebn.firealarm.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AutoScaleSquareRelativeLayout extends RelativeLayout {

  public AutoScaleSquareRelativeLayout(Context context) {
    super(context);
  }

  public AutoScaleSquareRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AutoScaleSquareRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (widthMeasureSpec < heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    } else if (heightMeasureSpec < widthMeasureSpec) {
      super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

}
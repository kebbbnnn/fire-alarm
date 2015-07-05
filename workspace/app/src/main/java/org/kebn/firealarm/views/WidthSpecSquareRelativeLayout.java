package org.kebn.firealarm.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Creates a square relative layout depending on the width measure spec.
 *
 * @author null
 * @see AutoScaleSquareRelativeLayout
 * @deprecated Please use AutoScaleSquareRelativeLayout instead.
 */
public class WidthSpecSquareRelativeLayout extends RelativeLayout {

  public WidthSpecSquareRelativeLayout(Context context) {
    super(context);
  }

  public WidthSpecSquareRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public WidthSpecSquareRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
  }

}

package com.doomonafireball.betterpickers.radialtimepicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * @author Olexandr Tereshchuk - "Stanfy"
 * @since 20.05.14
 */
public class RadialPeriodSelectorView extends RadialSelectorView {

  private final RectF mCircleRect = new RectF();

  private int periodDegrees = 0;

  public RadialPeriodSelectorView(final Context context) {
    super(context);
  }

  public void setPeriodDegrees(final int periodDegrees) {
    this.periodDegrees = periodDegrees;
    invalidate();
  }

  @Override
  public void onDraw(Canvas canvas) {
    int viewWidth = getWidth();
    if (viewWidth == 0 || !mIsInitialized) {
      return;
    }

    super.onDraw(canvas);

    if (periodDegrees > 0) {
      int lineLength = mLineLength;
      double endAngle = mSelectionRadians + Math.toRadians(periodDegrees);
      int pointX = mXCenter + (int) (lineLength * Math.sin(endAngle));
      int pointY = mYCenter - (int) (lineLength * Math.cos(endAngle));

      canvas.drawLine(mXCenter, mYCenter, pointX, pointY, mPaint);

      mCircleRect.set(mXCenter - mLineLength, mYCenter - mLineLength, mXCenter + mLineLength, mYCenter + mLineLength);

      mPaint.setAlpha(100);
      canvas.drawArc(mCircleRect, (float) Math.toDegrees(mSelectionRadians) - periodDegrees, periodDegrees, true, mPaint);
    }
  }

  @Override
  boolean shouldDrawSelection() {
    return periodDegrees == 0;
  }
}

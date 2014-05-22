package com.doomonafireball.betterpickers.radialtimepicker;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @author Olexandr Tereshchuk - "Stanfy"
 * @since 21.05.14
 */
public class RadialPeriodPickerLayout extends RadialPickerLayout {

  public RadialPeriodPickerLayout(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected RadialSelectorView newRadialSelectorView(final Context context) {
    return new RadialPeriodSelectorView(context);
  }

  public void setPeriod(final int period) {
    if (period >= 60) {
      final int degrees = ((period / 60) % 12) * HOUR_VALUE_TO_DEGREES_STEP_SIZE;
      ((RadialPeriodSelectorView) mHourRadialSelectorView).setPeriodDegrees(degrees);
      ((RadialPeriodSelectorView) mMinuteRadialSelectorView).setPeriodDegrees(0);
    } else {
      final int degrees = period * MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
      ((RadialPeriodSelectorView) mHourRadialSelectorView).setPeriodDegrees(0);
      ((RadialPeriodSelectorView) mMinuteRadialSelectorView).setPeriodDegrees(degrees);
    }
  }

}

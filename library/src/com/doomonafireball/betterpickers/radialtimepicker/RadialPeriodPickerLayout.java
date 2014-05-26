package com.doomonafireball.betterpickers.radialtimepicker;

import android.content.Context;
import android.util.AttributeSet;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Olexandr Tereshchuk - "Stanfy"
 * @since 21.05.14
 */
public class RadialPeriodPickerLayout extends RadialPickerLayout {

  /** Valid times. */
  private TreeMap<Integer, TreeSet<Integer>> validTimes;

  public RadialPeriodPickerLayout(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    blockPickerViewUpdate = true;
  }

  @Override
  protected RadialSelectorView newRadialSelectorView(final Context context) {
    return new RadialPeriodSelectorView(context);
  }

  public void setTimeWindows(final int period, final TreeMap<Integer, TreeSet<Integer>> validTimes) {
    final RadialPeriodSelectorView hourRadialSelectorView = (RadialPeriodSelectorView) mHourRadialSelectorView;
    final RadialPeriodSelectorView minuteRadialSelectorView = (RadialPeriodSelectorView) mMinuteRadialSelectorView;

    if (period >= 60) {
      final int degrees = ((period / 60) % 12) * HOUR_VALUE_TO_DEGREES_STEP_SIZE;
      hourRadialSelectorView.setPeriodDegrees(degrees);
      minuteRadialSelectorView.setPeriodDegrees(0);
    } else {
      final int degrees = period * MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
      hourRadialSelectorView.setPeriodDegrees(0);
      minuteRadialSelectorView.setPeriodDegrees(degrees);
    }

    this.validTimes = validTimes;

    final boolean[] enabledTexts = new boolean[12];
    final boolean[] enabledInnerTexts = new boolean[12];
    for (int i = 0; i < enabledTexts.length; i++) {
      enabledTexts[i] = validTimes.get(mIs24HourMode ? hours24[i] : hours[i]) != null;
      if (mIs24HourMode) {
        enabledInnerTexts[i] = validTimes.get(hours[i]) != null;
      }
    }
    mHourRadialTextsView.setEnabledTexts(enabledTexts, enabledInnerTexts);
  }

  public void updateValues(final int hour, final int minute, final int amPm) {
    blockPickerViewUpdate = false;

    setAmOrPm(amPm);
    setTime(hour, minute);

    final boolean[] enabledTexts = new boolean[12];
    final TreeSet<Integer> validMinutes = validTimes.get(hour);
    if (validMinutes != null && validMinutes.size() > 0) {
      for (int i = 0; i < minutes.length; i++) {
        enabledTexts[i] = validMinutes.contains(minutes[i]);
      }
    }
    mMinuteRadialTextsView.setEnabledTexts(enabledTexts, null);

    blockPickerViewUpdate = true;
  }

}

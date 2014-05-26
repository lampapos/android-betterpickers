package com.doomonafireball.betterpickers.radialtimepicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.doomonafireball.betterpickers.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Olexandr Tereshchuk - "Stanfy"
 * @since 21.05.14
 */
public class RadialPeriodTimePickerDialog extends RadialTimePickerDialog {

  private static final String KEY_START_TIME = "date_start";
  private static final String KEY_END_TIME = "date_end";
  private static final String KEY_PERIOD = "period";

  private TextView mHourEndView;
  private TextView mHourSpaceEndView;
  private TextView mMinuteEndView;
  private TextView mMinuteSpaceEndView;
  private TextView mAmPmTextEndView;

  private int startHour, startMinute;
  private int endHour, endMinute;
  private Date startTime, endTime;
  private int period;
  private final ArrayList<Date> windows = new ArrayList<>();
  private boolean crossDay = false;

  private final Calendar calendar = Calendar.getInstance();

  private final TreeMap<Integer, TreeSet<Integer>> validTimes = new TreeMap<>();


  public static RadialPeriodTimePickerDialog newInstance(OnTimeSetListener callback,
                                                   int hourOfDay, int minute, boolean is24HourMode, Date date,
                                                   Date startTime, Date endTime, int period) {
    RadialPeriodTimePickerDialog ret = new RadialPeriodTimePickerDialog();
    ret.initialize(callback, hourOfDay, minute, is24HourMode, date, startTime, endTime, period);
    return ret;
  }

  public void initialize(final OnTimeSetListener callback, final int hourOfDay, final int minute, final boolean is24HourMode, final Date date,
                         final Date startTime, final Date endTime, final int period) {
    super.initialize(callback, hourOfDay, minute, is24HourMode, date);

    initTimeWindows(startTime, endTime, period);
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null && savedInstanceState.containsKey(KEY_PERIOD)) {
      initTimeWindows(new Date(savedInstanceState.getLong(KEY_START_TIME)), new Date(savedInstanceState.getLong(KEY_END_TIME)), savedInstanceState.getInt(KEY_PERIOD));
    }
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mTimePicker != null) {
      outState.putLong(KEY_START_TIME, startTime.getTime());
      outState.putLong(KEY_END_TIME, endTime.getTime());
      outState.putInt(KEY_PERIOD, period);
    }
  }

  private void initTimeWindows(final Date startTime, final Date endTime, final int period) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.period = period;

    calendar.setTime(this.startTime);
    final int dayStart = calendar.get(Calendar.DAY_OF_YEAR);
    startHour = calendar.get(Calendar.HOUR_OF_DAY);
    startMinute = calendar.get(Calendar.MINUTE);
    calendar.setTime(this.endTime);
    final int dayEnd = calendar.get(Calendar.DAY_OF_YEAR);
    endHour = calendar.get(Calendar.HOUR_OF_DAY);
    endMinute = calendar.get(Calendar.MINUTE);

    if (endTime.before(startTime)) {
      this.endTime = new Date(endTime.getTime() + 24L * 60L * 60L * 1000L);
      this.crossDay = true;
    } else {
      this.crossDay = dayStart != dayEnd;
    }

    calendar.setTime(this.startTime);
    calendar.clear(Calendar.SECOND);
    calendar.clear(Calendar.MILLISECOND);

    final int maxDelta = 60 * 1000 - 1;
    windows.clear();
    validTimes.clear();
    while (this.endTime.getTime() - calendar.getTimeInMillis() > maxDelta) {
      windows.add(calendar.getTime());

      for (int i = -1; i < period; i++) {
        if (i >= 0) {
          calendar.add(Calendar.MINUTE, 1);
        }
        Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
        TreeSet<Integer> minutes = validTimes.get(hour);
        if (minutes == null) {
          minutes = new TreeSet<>();
          validTimes.put(hour, minutes);
        }
        minutes.add(calendar.get(Calendar.MINUTE));
      }
    }
  }

  @Override
  protected int getLayoutId() {
    return R.layout.radial_period_time_picker_dialog;
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    mHourEndView = (TextView) view.findViewById(R.id.hours_end);
    mHourSpaceEndView = (TextView) view.findViewById(R.id.hour_space_end);
    mMinuteSpaceEndView = (TextView) view.findViewById(R.id.minutes_space_end);
    mMinuteEndView = (TextView) view.findViewById(R.id.minutes_end);
    mAmPmTextEndView = (TextView) view.findViewById(R.id.ampm_label_end);

    mHourEndView.setTextColor(mUnselectedColor);
    mMinuteEndView.setTextColor(mUnselectedColor);

    if (mIs24HourMode) {
      mAmPmTextEndView.setVisibility(View.GONE);
    } else {
      mAmPmTextEndView.setVisibility(View.VISIBLE);
    }

    ((RadialPeriodPickerLayout) mTimePicker).setTimeWindows(period, validTimes);

    return view;
  }

  private void superSetHour(final int value, final boolean announce) {
    super.setHour(value, announce);
  }
  @Override
  void setHour(final int value, final boolean announce) {
    updatePeriod();
  }

  private void superSetMinute(final int value) {
    super.setMinute(value);
  }
  @Override
  void setMinute(final int value) {
    updatePeriod();
  }

  private void superUpdateAmPmDisplay(final int amOrPm) {
    super.updateAmPmDisplay(amOrPm);
  }
  @Override
  void updateAmPmDisplay(final int amOrPm) {
    updatePeriod();
  }

  private void updatePeriod() {
    calendar.setTime(mDate);
    calendar.set(Calendar.MINUTE, mTimePicker.getMinutes());
    calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getHours());

    if (crossDay && calendar.get(Calendar.HOUR_OF_DAY) < endHour) {
      calendar.add(Calendar.DAY_OF_YEAR, 1);
    }

    Date selectedWindow = null;
    for (final Date window : windows) {
      final long time = calendar.getTimeInMillis();
      final long windowStart = window.getTime();
      if (time >= windowStart && time <= windowStart + period * 60 * 1000) {
        selectedWindow = window;
        break;
      }
    }
    if (selectedWindow == null) { selectedWindow = windows.get(windows.size() - 1); }

    calendar.setTime(selectedWindow);

    final int windowHourStart = calendar.get(Calendar.HOUR_OF_DAY);
    final int windowMinuteStart = calendar.get(Calendar.MINUTE);

    ((RadialPeriodPickerLayout) mTimePicker).updateValues(windowHourStart, windowMinuteStart,
        (mIs24HourMode ? mTimePicker.getIsCurrentlyAmOrPm() : (windowHourStart < 12 ? AM : PM)));

    superSetHour(windowHourStart, true);
    superSetMinute(windowMinuteStart);
    superUpdateAmPmDisplay(mTimePicker.getIsCurrentlyAmOrPm());

    calendar.add(Calendar.MINUTE, period);

    CharSequence text = formatHour(calendar.get(Calendar.HOUR_OF_DAY));
    mHourEndView.setText(text);
    mHourSpaceEndView.setText(text);

    text = formatMinute(calendar.get(Calendar.MINUTE));
    mMinuteEndView.setText(text);
    mMinuteSpaceEndView.setText(text);

    if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
      mAmPmTextEndView.setText(mAmText);
    } else {
      mAmPmTextEndView.setText(mPmText);
    }
  }
}

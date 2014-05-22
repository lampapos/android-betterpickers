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
    while (this.endTime.getTime() - calendar.getTimeInMillis() > maxDelta) {
      windows.add(calendar.getTime());
      calendar.add(Calendar.MINUTE, this.period);
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

    ((RadialPeriodPickerLayout) mTimePicker).setPeriod(period);

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

    Date window = null;
    for (int i = 0; i < windows.size(); i++) {
      if (windows.get(i).getTime() > calendar.getTimeInMillis()) {
        window = windows.get(i);
        break;
      }
    }
    if (window == null) { window = windows.get(windows.size() - 1); }

    calendar.setTime(window);

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

    calendar.add(Calendar.MINUTE, -period);
    final int windowHourStart = calendar.get(Calendar.HOUR_OF_DAY);
    final int windowMinuteStart = calendar.get(Calendar.MINUTE);
    mTimePicker.setAmOrPm(mIs24HourMode ? mTimePicker.getIsCurrentlyAmOrPm() : (windowHourStart < 12 ? AM : PM));
    mTimePicker.setTime(windowHourStart, windowMinuteStart);

    superSetHour(windowHourStart, true);
    superSetMinute(windowMinuteStart);
    superUpdateAmPmDisplay(mTimePicker.getIsCurrentlyAmOrPm());
  }
}

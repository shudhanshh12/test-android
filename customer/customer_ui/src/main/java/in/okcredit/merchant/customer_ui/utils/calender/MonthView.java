package in.okcredit.merchant.customer_ui.utils.calender;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import in.okcredit.backend._offline.model.DueInfo;
import in.okcredit.merchant.customer_ui.R;
import io.reactivex.disposables.Disposable;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import tech.okcredit.android.base.utils.DateTimeUtils;

public class MonthView extends LinearLayout implements DateClickListener {
    HashSet<MonthRespository.OKCSelectedDate> okcSelectedDateHashSet = new LinkedHashSet<>();
    private TextView month;
    private TextView yearTextView;
    private TextView date;
    private TextView day;
    private TextView fullMonthName;
    private DataAdapter adpater;
    private CapturedDate selectedDate;
    private GridView gridView;
    private CapturedDate previousSelectedDate;
    private OnDateSelectListener onDateSelectListener;
    private OnDateClearListener onDateClearListener;
    private Disposable internetDisposable;
    private View view;

    public MonthView(Context context) {
        super(context);
        initView(context);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        view = LinearLayout.inflate(context, R.layout.item_month, this);
        month = view.findViewById(R.id.month);
        yearTextView = view.findViewById(R.id.year_name);
        date = view.findViewById(R.id.date);
        day = view.findViewById(R.id.day);
        fullMonthName = view.findViewById(R.id.full_month_name);
        MonthRespository monthRespository = new MonthRespository();
        final MonthRespository.OKCMonth[] monthData = {monthRespository.getCurrentMonthData()};
        gridView = view.findViewById(R.id.grid_view);
        adpater = new DataAdapter(context, R.layout.single_cell, this);
        gridView.setAdapter(adpater);
        adpater.appendData(monthData);
        setData(monthData[0]);
        view.findViewById(R.id.next)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                monthData[0] = monthRespository.getNextMonthData();
                                adpater.appendData(monthData);
                                fullMonthName.setText(
                                        monthData[0].monthName + " " + monthData[0].yearName);
                            }
                        });
        view.findViewById(R.id.previous)
                .setOnClickListener(
                        view1 -> {
                            monthData[0] = monthRespository.getPreviousMonthData();
                            adpater.appendData(monthData);
                            fullMonthName.setText(
                                    monthData[0].monthName + " " + monthData[0].yearName);
                        });
    }

    private void setData(MonthRespository.OKCMonth monthDatum) {
        month.setText(monthDatum.shortMonthName);
        yearTextView.setText(monthDatum.yearName);
        date.setText(monthDatum.dateName);
        day.setText(monthDatum.dayName + ", ");
        fullMonthName.setText(monthDatum.monthName + " " + monthDatum.yearName);
    }

    public void setReleventDate(DateTime dateTime) {
        MonthRespository monthRespository = new MonthRespository();
        int monthInt = CalenderUtils.getMonthInInt(DateTimeUtils.getMonth(dateTime));
        if (monthInt != -1) {
            final MonthRespository.OKCMonth[] monthData = {
                monthRespository.getReleventMonthData(monthInt, dateTime.getYear())
            };
            gridView.setAdapter(adpater);
            adpater.appendData(monthData);
            setData(monthData[0]);

            view.findViewById(R.id.next)
                    .setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    monthData[0] = monthRespository.getNextMonthData();
                                    adpater.appendData(monthData);
                                    fullMonthName.setText(
                                            monthData[0].monthName + " " + monthData[0].yearName);
                                }
                            });
            view.findViewById(R.id.previous)
                    .setOnClickListener(
                            view1 -> {
                                monthData[0] = monthRespository.getPreviousMonthData();
                                adpater.appendData(monthData);
                                fullMonthName.setText(
                                        monthData[0].monthName + " " + monthData[0].yearName);
                            });
        }
    }

    @Override
    public void onDateClicked(OKCDate okcDate) {
        setSelectedDate(new CapturedDate(okcDate, CapturedDate.DateStatus.ADDED));
        onDateSelectListener.onDateSelected(getSelectedDate());
    }

    @Override
    protected void onDetachedFromWindow() {
        if (internetDisposable != null) {
            internetDisposable.dispose();
        }
        super.onDetachedFromWindow();
    }

    private boolean dateInSelectedDates(
            HashSet<MonthRespository.OKCSelectedDate> selectedDates, OKCDate date) {
        for (MonthRespository.OKCSelectedDate okcSelectedDate : selectedDates) {
            if (date.getTimeInMillis()
                    == okcSelectedDate.dueAt.withTimeAtStartOfDay().getMillis()) {
                return true;
            }
        }
        return false;
    }

    public CapturedDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(CapturedDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void refresh() {
        okcSelectedDateHashSet.clear();
        adpater.resetSelectedDates();
    }

    public CapturedDate getPreselectedDate() {
        return previousSelectedDate;
    }

    public void setOnDateSelectListener(
            @NotNull MonthView.OnDateSelectListener onDateSelectListener) {
        this.onDateSelectListener = onDateSelectListener;
    }

    public void setOnDateClearListner(@NotNull MonthView.OnDateClearListener onDateClearListener) {
        this.onDateClearListener = onDateClearListener;
    }

    public void setDueInfo(@NotNull DueInfo it) {
        if (it.isDueActive() && it.getActiveDate() != null) {
            okcSelectedDateHashSet.add(
                    new MonthRespository.OKCSelectedDate(
                            it.getActiveDate(),
                            STATUS.ACTIVE.value,
                            INVALIDATION_REASON.UNKOWN.value,
                            false));
            CapturedDate mdate =
                    new CapturedDate(
                            new OKCDate(
                                    DateTimeUtils.getDateFromMillis(it.getActiveDate()),
                                    it.getActiveDate().getMillis()),
                            CapturedDate.DateStatus.ADDED);
            setSelectedDate(mdate);
        }
        adpater.setPreSelectedDates(okcSelectedDateHashSet);
    }

    enum STATUS {
        UNKOWN(0),
        ACTIVE(1),
        INVALIDATED(2);
        private final int value;

        STATUS(int value) {
            this.value = value;
        }
    }

    enum INVALIDATION_REASON {
        UNKOWN(0);
        private final int value;

        INVALIDATION_REASON(int value) {
            this.value = value;
        }
    }

    public static class CapturedDate {
        public DateStatus dateStatus;
        public OKCDate okcDate;

        public CapturedDate(OKCDate okcDate, DateStatus aNew) {
            this.okcDate = okcDate;
            this.dateStatus = aNew;
        }

        public enum DateStatus {
            ADDED,
            DELETED
        }
    }

    public interface OnDateSelectListener {
        void onDateSelected(@NotNull CapturedDate capturedDate);
    }

    public interface OnDateClearListener {
        void onDateCleared(@NotNull CapturedDate capturedDate);
    }
}

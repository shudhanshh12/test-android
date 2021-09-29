package in.okcredit.merchant.customer_ui.utils.calender;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import in.okcredit.merchant.customer_ui.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.joda.time.DateTime;
import tech.okcredit.android.base.utils.DateTimeUtils;

class DataAdapter extends ArrayAdapter {

    private final DateClickListener dateClickListener;
    private HashSet<MonthRespository.OKCSelectedDate> selectedDates = new HashSet<>();
    private LayoutInflater mInflater;
    private List<OKCDate> monthlyDates = new ArrayList<>();

    DataAdapter(Context context, int singleCell, DateClickListener dateClickListener) {
        super(context, singleCell);
        mInflater = LayoutInflater.from(context);
        this.dateClickListener = dateClickListener;
        selectedDates.clear();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        OKCDate mDate = monthlyDates.get(position);
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.single_cell, parent, false);
        }
        TextView cellNumber = view.findViewById(R.id.date);
        ImageView background = view.findViewById(R.id.background);
        cellNumber.setText("");
        background.setImageDrawable(
                view.getContext().getResources().getDrawable(R.drawable.circle_filled_white));
        cellNumber.setTextColor(view.getContext().getResources().getColor(R.color.white));
        if (mDate.getData() != -1) {
            cellNumber.setText(String.valueOf(mDate.getData()));
            cellNumber.setTextColor(view.getContext().getResources().getColor(R.color.black));
            background.setImageDrawable(
                    view.getContext().getResources().getDrawable(R.drawable.circle_filled_white));

            if (DateTimeUtils.isCurrentOrPassedDate(mDate.getTimeInMillis())) {
                cellNumber.setTextColor(
                        view.getContext().getResources().getColor(R.color.black_88));
            }

            if (mDate.isFutureDate()) {
                if (mDate.getStatus() == MonthView.STATUS.ACTIVE.ordinal()) {
                    cellNumber.setTextColor(
                            view.getContext().getResources().getColor(R.color.white));
                    background.setImageDrawable(
                            view.getContext()
                                    .getResources()
                                    .getDrawable(R.drawable.circle_filled_green));
                }
            }

            if (mDate.getStatus() == MonthView.STATUS.ACTIVE.ordinal()) {
                if (DateTimeUtils.getMonth(mDate.getTimeInMillis())
                        != DateTime.now().getMonthOfYear())
                    if (DateTimeUtils.isCurrentOrPassedDate(mDate.getTimeInMillis())) {
                        cellNumber.setTextColor(
                                view.getContext().getResources().getColor(R.color.white));
                        background.setImageDrawable(
                                view.getContext()
                                        .getResources()
                                        .getDrawable(R.drawable.circle_filled_skyblue));
                    }
            }
        }
        view.setOnClickListener(
                view1 -> {
                    if (mDate.getData() != -1 && mDate.isFutureDate()) {
                        dateClickListener.onDateClicked(mDate);
                    } else if (mDate.getData() != -1
                            && DateTimeUtils.isCurrentDate(new DateTime(mDate.getTimeInMillis()))) {
                        Toast.makeText(
                                        getContext(),
                                        getContext().getString(R.string.cannot_select_current_date),
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else if (mDate.getData() != -1
                            && DateTimeUtils.isCurrentOrPassedDate(mDate.getTimeInMillis())) {
                        Toast.makeText(
                                        getContext(),
                                        getContext().getString(R.string.cannot_select_past_date),
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        return view;
    }

    public int getCount() {
        return monthlyDates.size();
    }

    public void appendData(MonthRespository.OKCMonth[] monthData) {
        monthlyDates.clear();
        monthlyDates.addAll(monthData[0].dates);
        setPreSelectedDates(selectedDates);
        notifyDataSetChanged();
    }

    public void setPreSelectedDates(HashSet<MonthRespository.OKCSelectedDate> dueViewModelList) {
        selectedDates.addAll(dueViewModelList);

        for (int i = 0; i < monthlyDates.size(); i++) {
            monthlyDates.get(i).setStatus(0);
            monthlyDates.get(i).setInvalidationReason(0);
            monthlyDates.get(i).setDueReminderSent(false);
            for (MonthRespository.OKCSelectedDate dueViewModel : selectedDates) {
                if (monthlyDates.get(i).getTimeInMillis()
                        == dueViewModel.getDueAt().withTimeAtStartOfDay().getMillis()) {
                    monthlyDates.get(i).setStatus(dueViewModel.getStatus());
                    monthlyDates.get(i).setInvalidationReason(dueViewModel.getInvalidationReason());
                    monthlyDates.get(i).setDueReminderSent(dueViewModel.isDueReminderSent());
                }
            }
        }

        notifyDataSetChanged();
    }

    public void resetSelectedDates() {
        selectedDates.clear();
    }
}

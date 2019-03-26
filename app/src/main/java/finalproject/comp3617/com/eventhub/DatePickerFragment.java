package finalproject.comp3617.com.eventhub;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public DatePickerFragment() { }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        TextView evDate = getActivity().findViewById(R.id.dateTxt);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,dayOfMonth,8,0,0);
        String datePicked = App.Constants.df.format(calendar.getTime());
        Date dateTemp = App.Constants.parseFirebaseDate(datePicked);
        evDate.setText(datePicked);
        String dateConfirm = getResources().getString(R.string.dateConfirm);
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                dateConfirm, Snackbar.LENGTH_LONG).show();
        ((EventDetailsActivity) getContext()).saveSelectedDate(dateTemp);
    }
}

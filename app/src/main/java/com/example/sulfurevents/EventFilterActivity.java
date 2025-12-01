package com.example.sulfurevents;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for filtering events based on keyword, location, and date range.
 * Users can select which filters to apply and set filter values.
 */
public class EventFilterActivity extends AppCompatActivity {

    private EditText searchKeywordEdit;
    private EditText locationFilterEdit;
    private EditText startDateEdit;
    private EditText endDateEdit;

    private CheckBox filterByKeywordCheckbox;
    private CheckBox filterByLocationCheckbox;
    private CheckBox filterByDateCheckbox;

    private Button applyFiltersButton;
    private Button clearFiltersButton;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_filter);

        // Initialize views
        searchKeywordEdit = findViewById(R.id.search_keyword_edit);
        locationFilterEdit = findViewById(R.id.location_filter_edit);
        startDateEdit = findViewById(R.id.start_date_edit);
        endDateEdit = findViewById(R.id.end_date_edit);

        filterByKeywordCheckbox = findViewById(R.id.filter_by_keyword_checkbox);
        filterByLocationCheckbox = findViewById(R.id.filter_by_location_checkbox);
        filterByDateCheckbox = findViewById(R.id.filter_by_date_checkbox);

        applyFiltersButton = findViewById(R.id.apply_filters_button);
        clearFiltersButton = findViewById(R.id.clear_filters_button);

        ImageButton back = findViewById(R.id.btnBackFilters);
        back.setOnClickListener(v -> finish());

        // Load existing filter values if passed from EntrantActivity
        Intent intent = getIntent();
        if (intent.hasExtra("filterKeyword")) {
            String keyword = intent.getStringExtra("filterKeyword");
            searchKeywordEdit.setText(keyword);
            filterByKeywordCheckbox.setChecked(true);
        }

        if (intent.hasExtra("filterLocation")) {
            String location = intent.getStringExtra("filterLocation");
            locationFilterEdit.setText(location);
            filterByLocationCheckbox.setChecked(true);
        }

        if (intent.hasExtra("filterStartDate") && intent.hasExtra("filterEndDate")) {
            String startDate = intent.getStringExtra("filterStartDate");
            String endDate = intent.getStringExtra("filterEndDate");
            startDateEdit.setText(startDate);
            endDateEdit.setText(endDate);
            filterByDateCheckbox.setChecked(true);
        }

        // Setup date pickers
        startDateEdit.setOnClickListener(v -> showDatePickerDialog(true));
        endDateEdit.setOnClickListener(v -> showDatePickerDialog(false));

        // Disable/enable edit fields based on checkboxes
        filterByKeywordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            searchKeywordEdit.setEnabled(isChecked);
            if (!isChecked) {
                searchKeywordEdit.setText("");
            }
        });

        filterByLocationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            locationFilterEdit.setEnabled(isChecked);
            if (!isChecked) {
                locationFilterEdit.setText("");
            }
        });

        filterByDateCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            startDateEdit.setEnabled(isChecked);
            endDateEdit.setEnabled(isChecked);
            if (!isChecked) {
                startDateEdit.setText("");
                endDateEdit.setText("");
            }
        });

        // Set initial state
        searchKeywordEdit.setEnabled(filterByKeywordCheckbox.isChecked());
        locationFilterEdit.setEnabled(filterByLocationCheckbox.isChecked());
        startDateEdit.setEnabled(filterByDateCheckbox.isChecked());
        endDateEdit.setEnabled(filterByDateCheckbox.isChecked());

        // Apply filters button
        applyFiltersButton.setOnClickListener(v -> applyFilters());

        // Clear filters button
        clearFiltersButton.setOnClickListener(v -> clearFilters());
    }

    /**
     * Shows a date picker dialog for selecting start or end date
     */
    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String dateString = dateFormat.format(calendar.getTime());
                    if (isStartDate) {
                        startDateEdit.setText(dateString);
                    } else {
                        endDateEdit.setText(dateString);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    /**
     * Applies the selected filters and returns to EntrantActivity
     */
    private void applyFilters() {
        Intent resultIntent = new Intent();

        // Only pass filter values if their checkbox is checked
        if (filterByKeywordCheckbox.isChecked()) {
            String keyword = searchKeywordEdit.getText().toString().trim();
            if (!keyword.isEmpty()) {
                resultIntent.putExtra("filterKeyword", keyword);
            }
        }

        if (filterByLocationCheckbox.isChecked()) {
            String location = locationFilterEdit.getText().toString().trim();
            if (!location.isEmpty()) {
                resultIntent.putExtra("filterLocation", location);
            }
        }

        if (filterByDateCheckbox.isChecked()) {
            String startDate = startDateEdit.getText().toString().trim();
            String endDate = endDateEdit.getText().toString().trim();
            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                resultIntent.putExtra("filterStartDate", startDate);
                resultIntent.putExtra("filterEndDate", endDate);
            }
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Clears all filters and returns to EntrantActivity
     */
    private void clearFilters() {
        Intent resultIntent = new Intent();
        // Send empty intent to clear filters
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
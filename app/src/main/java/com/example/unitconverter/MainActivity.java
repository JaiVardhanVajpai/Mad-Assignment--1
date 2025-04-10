package com.example.unitconverter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private EditText editTextValue;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private Button buttonConvert;
    private TextView textViewResult;
    private RadioGroup radioGroupTheme;
    private RadioButton radioButtonLight;
    private RadioButton radioButtonDark;
    private Button buttonApplyTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        editTextValue = findViewById(R.id.editTextValue);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        buttonConvert = findViewById(R.id.buttonConvert);
        textViewResult = findViewById(R.id.textViewResult);
        radioGroupTheme = findViewById(R.id.radioGroupTheme);
        radioButtonLight = findViewById(R.id.radioButtonLight);
        radioButtonDark = findViewById(R.id.radioButtonDark);
        buttonApplyTheme = findViewById(R.id.buttonApplyTheme);

        // Set up the unit spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.length_units,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Set default selections
        spinnerFrom.setSelection(0);  // Feet
        spinnerTo.setSelection(2);    // Centimeters

        // Set up the convert button with loading animation
        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a brief "loading" message
                textViewResult.setText("Converting...");

                // Disable button during "conversion"
                buttonConvert.setEnabled(false);

                // Add a small delay to simulate processing (optional for visual effect)
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // Perform the actual conversion
                                convertUnits();

                                // Re-enable the button
                                buttonConvert.setEnabled(true);
                            }
                        },
                        500);  // 500ms delay
            }
        });

        // Load current theme setting
        loadThemeSetting();

        // Set up the apply theme button
        buttonApplyTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable button during theme change
                buttonApplyTheme.setEnabled(false);
                buttonApplyTheme.setText("Applying...");

                // Add a small delay for visual effect
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                saveAndApplyTheme();
                                buttonApplyTheme.setEnabled(true);
                                buttonApplyTheme.setText("Apply Theme");
                            }
                        },
                        500);  // 500ms delay
            }
        });
    }

    // Method to perform unit conversion
    private void convertUnits() {
        String valueStr = editTextValue.getText().toString();

        if (valueStr.isEmpty()) {
            textViewResult.setText("Please enter a value");
            return;
        }

        try {
            double value = Double.parseDouble(valueStr);
            String fromUnit = spinnerFrom.getSelectedItem().toString();
            String toUnit = spinnerTo.getSelectedItem().toString();

            // Convert to meters first (as base unit)
            double valueInMeters = 0;

            if (fromUnit.equals("Feet")) {
                valueInMeters = value * 0.3048;
            } else if (fromUnit.equals("Inches")) {
                valueInMeters = value * 0.0254;
            } else if (fromUnit.equals("Centimeters")) {
                valueInMeters = value * 0.01;
            } else if (fromUnit.equals("Meters")) {
                valueInMeters = value;
            } else if (fromUnit.equals("Yards")) {
                valueInMeters = value * 0.9144;
            }

            // Convert from meters to target unit
            double result = 0;

            if (toUnit.equals("Feet")) {
                result = valueInMeters / 0.3048;
            } else if (toUnit.equals("Inches")) {
                result = valueInMeters / 0.0254;
            } else if (toUnit.equals("Centimeters")) {
                result = valueInMeters / 0.01;
            } else if (toUnit.equals("Meters")) {
                result = valueInMeters;
            } else if (toUnit.equals("Yards")) {
                result = valueInMeters / 0.9144;
            }

            // Format and display the result
            String resultText = String.format("%.4f %s = %.4f %s", value, fromUnit, result, toUnit);
            textViewResult.setText(resultText);

        } catch (NumberFormatException e) {
            textViewResult.setText("Please enter a valid number");
        }
    }

    // Load current theme setting
    private void loadThemeSetting() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        int currentTheme = sharedPreferences.getInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            radioButtonLight.setChecked(true);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            radioButtonDark.setChecked(true);
        }
    }

    // Save and apply theme changes
    private void saveAndApplyTheme() {
        int themeMode;

        if (radioButtonLight.isChecked()) {
            themeMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (radioButtonDark.isChecked()) {
            themeMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }

        // Save the theme preference
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("themeMode", themeMode);
        editor.apply();

        // Apply the theme
        AppCompatDelegate.setDefaultNightMode(themeMode);

        Toast.makeText(this, "Theme applied successfully", Toast.LENGTH_SHORT).show();
    }
}

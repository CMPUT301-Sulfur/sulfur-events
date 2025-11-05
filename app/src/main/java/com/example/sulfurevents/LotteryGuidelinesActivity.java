package com.example.sulfurevents;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LotteryGuidelinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottery_guidelines);

        TextView tv = findViewById(R.id.tv_lottery_text);
        tv.setText(
                "Lottery Guidelines:\n" +
                        "1. Entrants on the waiting list at the time of the draw are eligible.\n" +
                        "2. Organizer performs a random draw to select participants.\n" +
                        "3. Duplicate/invalid entries may be removed.\n" +
                        "4. If selected entrants decline or are removed, organizer may re-draw.\n" +
                        "5. Being on the waiting list does not guarantee a spot."
        );
    }
}

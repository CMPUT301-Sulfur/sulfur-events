package com.example.sulfurevents;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Simple screen that shows the entrant how the lottery system works.
 *
 * <p>This activity:
 * <ul>
 *     <li>Inflates {@code activity_lottery_guidelines}</li>
 *     <li>Fills a {@link TextView} with static help text</li>
 *     <li>Closes itself when the back button is pressed</li>
 * </ul>
 *
 * It does not read or write to Firestore – it’s just informational.
 */
public class LotteryGuidelinesActivity extends AppCompatActivity {

    private TextView lotteryText;
    private Button backButton;

    /**
     * Initializes the view and sets the static guidelines text.
     *
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottery_guidelines);

        lotteryText = findViewById(R.id.lottery_text);
        backButton = findViewById(R.id.back_button);

        // Set the lottery guidelines text
        String guidelines = "Lottery System Guidelines\n\n" +
                "1. Join the Waiting List\n" +
                "   - Browse available events and tap 'Join Waiting List'\n" +
                "   - You can join multiple event waiting lists\n\n" +
                "2. Lottery Selection Process\n" +
                "   - Organizers will run a lottery when registration closes\n" +
                "   - Selected entrants will be notified\n" +
                "   - Selection is random and fair\n\n" +
                "3. Waiting List Status\n" +
                "   - View your status on each waiting list\n" +
                "   - You can leave a waiting list at any time before the lottery\n\n" +
                "4. After Selection\n" +
                "   - If selected, you'll receive a notification\n" +
                "   - Accept or decline your spot promptly\n" +
                "   - Declined spots may go to waiting list members\n\n" +
                "5. Cancellation Policy\n" +
                "   - You can leave the waiting list anytime before selection\n" +
                "   - Once selected, follow the event's cancellation policy\n\n" +
                "Good luck with your event entries!";


        lotteryText.setText(guidelines);


        backButton.setOnClickListener(v -> finish());
    }
}
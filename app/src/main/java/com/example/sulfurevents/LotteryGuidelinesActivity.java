package com.example.sulfurevents;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The {@code LotteryGuidelinesActivity} class displays the event lottery
 * participation guidelines for entrants within the SulfurEvents app.
 * <p>
 * This screen provides users with detailed information about how the
 * lottery system works — including joining, selection, and cancellation policies.
 * <p>
 * The activity contains a text view showing the guidelines and a back button
 * that allows the user to return to the previous screen.
 *
 * <p>Associated layout: {@code activity_lottery_guidelines.xml}
 */
public class LotteryGuidelinesActivity extends AppCompatActivity {

    /** Displays the lottery system guidelines text. */
    private TextView lotteryText;

    /** Button that returns the user to the previous screen. */
    private Button backButton;


    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the UI elements, sets up the static lottery guidelines text,
     * and configures the back button behavior.
     *
     * @param savedInstanceState The saved instance state bundle (if any)
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

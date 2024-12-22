package com.suer.pomodoroapplication101;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private boolean isNightMode = false;
    private Chronometer timerTextView; // Chronometer olarak değiştirildi
    private boolean isRunning = false;
    private long totalTimeInMillis = 0;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout mainLayout = findViewById(R.id.main_layout);
        Button modeButton = findViewById(R.id.modeButton);
        Button setTimeButton = findViewById(R.id.set_time_button);
        Button startStopButton = findViewById(R.id.start_stop_button);
        Button resetButton = findViewById(R.id.reset_button);

        timerTextView = findViewById(R.id.timerTextView); // Chronometer alındı
        Drawable dayBackground = ContextCompat.getDrawable(this, R.drawable.day_background);
        Drawable nightBackground = ContextCompat.getDrawable(this, R.drawable.night_background);

        timerTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));

        // Gece ve gündüz modlarını ayarlama
        mainLayout.setBackground(dayBackground);
        modeButton.setText("Gece Moduna Geç");

        // Gece/Gündüz modu değiştirme
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable nextBackground = isNightMode ? dayBackground : nightBackground;
                modeButton.setText(isNightMode ? "Gece Moduna Geç" : "Gündüz Moduna Geç");
                animateBackgroundChange(mainLayout, nextBackground);
                isNightMode = !isNightMode;
            }
        });

        // Timer'a tıklama ile dakika ve saniye seçme işlemi
        timerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMinuteSecondPickerDialog();
            }
        });

        // Butonların işlevselliğini ayarlama
        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMinuteSecondPickerDialog();
            }
        });

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTimer(startStopButton);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer(startStopButton);
            }
        });
    }

    private void animateBackgroundChange(RelativeLayout mainLayout, Drawable nextBackground) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mainLayout, "alpha", 1f, 0f);
        fadeOut.setDuration(250);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mainLayout, "alpha", 0f, 1f);
        fadeIn.setDuration(250);

        fadeOut.start();
        fadeOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                mainLayout.setBackground(nextBackground);
                fadeIn.start();
            }
            @Override public void onAnimationStart(Animator animator) {}
            @Override public void onAnimationCancel(Animator animator) {}
            @Override public void onAnimationRepeat(Animator animator) {}
        });
    }

    private void showMinuteSecondPickerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.minute_second_picker, null);
        EditText minuteInput = dialogView.findViewById(R.id.minute_input);
        EditText secondInput = dialogView.findViewById(R.id.second_input);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Süreyi Ayarlayın")
                .setView(dialogView)
                .setPositiveButton("Tamam", (dialog, which) -> {
                    try {
                        int minutes = Integer.parseInt(minuteInput.getText().toString());
                        int seconds = Integer.parseInt(secondInput.getText().toString());

                        // Süre kontrolü
                        if (minutes < 25 || minutes > 120) {
                            Toast.makeText(MainActivity.this, "Süre 25 ile 120 dakika arasında olmalıdır.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (seconds >= 60) {
                            Toast.makeText(MainActivity.this, "Saniye 0-59 aralığında olmalıdır.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        setTimerTime(minutes, seconds);  // Timer'ı güncelle
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Geçersiz giriş!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void setTimerTime(int minutes, int seconds) {
        totalTimeInMillis = (minutes * 60 + seconds) * 1000;
        timeLeftInMillis = totalTimeInMillis;
        updateTimerText(); // Güncellenmiş zaman metni

        if (isRunning) {
            pauseTimer();  // Timer çalışıyorsa durdur
        }

        // Kronometreyi güncelle
        timerTextView.setBase(SystemClock.elapsedRealtime() + timeLeftInMillis);
        timerTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));

        // Kronometreyi göster
        timerTextView.setText(""); // İlk başta görünüm boş olacak
        timerTextView.setBase(SystemClock.elapsedRealtime() + timeLeftInMillis); // Temel zaman ayarını yap
        updateTimerText(); // Doğru zaman formatını göstermek için tekrar güncelle
    }


    private void toggleTimer(Button startStopButton) {
        if (isRunning) {
            pauseTimer();
            startStopButton.setText("Başlat");
        } else {
            startTimer();
            startStopButton.setText("Durdur");
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                Toast.makeText(MainActivity.this, "Tebrikler🥳", Toast.LENGTH_SHORT).show();
                // Zaman dolduğunda yapacağınız işlemler (örneğin, ses çalma) eklenebilir.
            }
        }.start();
        isRunning = true;
        timerTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isRunning = false;
    }

    private void resetTimer(Button startStopButton) {
        timeLeftInMillis = totalTimeInMillis;
        updateTimerText();
        if (isRunning) {
            pauseTimer();
        }
        startStopButton.setText("Başlat");
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
        timerTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
    }
}

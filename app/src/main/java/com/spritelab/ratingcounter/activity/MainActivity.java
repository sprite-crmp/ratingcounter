package com.spritelab.ratingcounter.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ratingcounter.R;
import com.google.android.material.badge.BadgeUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textInput, textScore, textInfo;
    private Button btnBackspace, btnClear, btnPaste, btnFife, btnFour, btnThree, btnTwo;
    private List<Integer> rating = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInput = findViewById(R.id.textInput);
        textScore = findViewById(R.id.textScore);
        textInfo = findViewById(R.id.textInfo);
        btnBackspace = findViewById(R.id.btnBackspace);
        btnClear = findViewById(R.id.btnClear);
        btnPaste = findViewById(R.id.btnPaste);
        btnFife = findViewById(R.id.btnFife);
        btnFour = findViewById(R.id.btnFour);
        btnThree = findViewById(R.id.btnThree);
        btnTwo = findViewById(R.id.btnTwo);

        updateText();
        onClickListener();
        onPressedBack();
    }

    private void onClickListener() {
        btnBackspace.setOnClickListener(v -> {
            btnAnim(v);
            if (!rating.isEmpty()) {
                rating.remove(rating.size() - 1);
                updateText();
            }
        });
        btnClear.setOnClickListener(v -> {
            btnAnim(v);
            rating.clear();
            updateText();
        });
        btnPaste.setOnClickListener(v -> {
            btnAnim(v);
            pasteFromClipboard();
            updateText();
        });
        textInput.setOnClickListener(v -> {
            btnAnim(v);
            copyFromClipboard();
        });
        setupScoreButton(btnFife, 5);
        setupScoreButton(btnFour, 4);
        setupScoreButton(btnThree, 3);
        setupScoreButton(btnTwo, 2);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupScoreButton(Button button, int score) {
        button.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler = new Handler();
            private Runnable runnable;
            private boolean isLongPressing = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isLongPressing = true;
                        addScore(score);
                        btnAnim(v);

                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (isLongPressing) {
                                    addScore(score);
                                    btnAnim(v);
                                    handler.postDelayed(this, 500);
                                }
                            }
                        };
                        handler.postDelayed(runnable, 500);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isLongPressing = false;
                        if (runnable != null) {
                            handler.removeCallbacks(runnable);
                        }
                        return true;
                }
                return false;
            }
        });

        button.setOnClickListener(v -> {
            btnAnim(v);
            addScore(score);
        });
    }

    private void addScore(Integer score) {
        rating.add(score);
        animateScoreText();
        updateText();
    }

    private void animateScoreText() {
        textScore.setScaleX(1.1f);
        textScore.setScaleY(1.1f);
        textScore.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();

        textInfo.setScaleX(1.1f);
        textInfo.setScaleY(1.1f);
        textInfo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(100)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();

        textInput.setScaleX(1.1f);
        textInput.setScaleY(1.1f);
        textInput.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    private void animateViewVisibility(View view, boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
            view.setScaleX(0.7f);
            view.setScaleY(0.7f);
            view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .start();
        } else {
            view.animate()
                    .alpha(0f)
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .setDuration(300)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    private void updateText() {
        if (!rating.isEmpty()) {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < rating.size(); i++) {
                text.append(rating.get(i));
                if (i < rating.size() - 1) {
                    text.append(" ");
                }
            }
            textInput.setText(text);

            int sumScore = 0;
            for (int grade : rating) {
                sumScore += grade;
            }
            float currentAverage = (float) sumScore / rating.size();
            String formattedAverage = String.format("%.2f", currentAverage);
            textScore.setText("Средний балл: " + formattedAverage);

            int roundedGrade = Math.round(currentAverage);
            StringBuilder infoText = new StringBuilder();

            if (roundedGrade < 5) {
                int need5For5 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 4.5f, 5);
                int need4For5 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 4.5f, 4);
                if (need5For5 > 0) infoText.append("До отметки 5 нужно получить ").append(need5For5).append(" ").append(getGradeWord(need5For5, 5)).append("\n");
                if (need4For5 > 0) infoText.append("До отметки 5 нужно получить ").append(need4For5).append(" ").append(getGradeWord(need4For5, 4)).append("\n");
            }

            if (roundedGrade < 4) {
                int need5For4 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 3.5f, 5);
                int need4For4 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 3.5f, 4);
                int need3For4 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 3.5f, 3);
                if (need5For4 > 0) infoText.append("До отметки 4 нужно получить ").append(need5For4).append(" ").append(getGradeWord(need5For4, 5)).append("\n");
                if (need4For4 > 0) infoText.append("До отметки 4 нужно получить ").append(need4For4).append(" ").append(getGradeWord(need4For4, 4)).append("\n");
                if (need3For4 > 0) infoText.append("До отметки 4 нужно получить ").append(need3For4).append(" ").append(getGradeWord(need3For4, 3)).append("\n");
            }

            if (roundedGrade < 3) {
                int need5For3 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 2.5f, 5);
                int need4For3 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 2.5f, 4);
                int need3For3 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 2.5f, 3);
                int need2For3 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 2.5f, 2);
                if (need5For3 > 0) infoText.append("До отметки 3 нужно получить ").append(need5For3).append(" ").append(getGradeWord(need5For3, 5)).append("\n");
                if (need4For3 > 0) infoText.append("До отметки 3 нужно получить ").append(need4For3).append(" ").append(getGradeWord(need4For3, 4)).append("\n");
                if (need3For3 > 0) infoText.append("До отметки 3 нужно получить ").append(need3For3).append(" ").append(getGradeWord(need3For3, 3)).append("\n");
                if (need2For3 > 0) infoText.append("До отметки 3 нужно получить ").append(need2For3).append(" ").append(getGradeWord(need2For3, 2)).append("\n");
            }

            if (roundedGrade < 3) {
                int need2For2 = calculateNeededWithSpecificGrade(sumScore, rating.size(), 1.5f, 2);
                if (need2For2 > 0) infoText.append("До отметки 2 нужно получить ").append(need2For2).append(" ").append(getGradeWord(need2For2, 2)).append("\n");
            }

            if (infoText.length() > 0) {
                textInfo.setText(infoText.toString().trim());
                animateViewVisibility(textInfo, true);
            } else {
                animateViewVisibility(textInfo, false);
            }

            animateViewVisibility(textScore, true);
        } else {
            textInput.setText("Ожидание ввода...");
            animateViewVisibility(textScore, false);
            animateViewVisibility(textInfo, false);
        }
    }

    private void copyFromClipboard() {
        String text = String.valueOf(textInput.getText());
        if (text.matches("[2-5\\s]+")) {
            new CountDownTimer(1000, 1000) {
                @Override
                public void onFinish() {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("ratingScore", text);
                    clipboardManager.setPrimaryClip(clipData);
                }

                @Override
                public void onTick(long millisUntilFinished) {

                }
            }.start();

        } else {
            Toast.makeText(this, "Введите оценки на клавиатуре", Toast.LENGTH_SHORT).show();
        }
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            CharSequence pasteData = item.getText();

            if (pasteData != null) {
                String clipboardText = pasteData.toString();
                parseAndAddGrades(clipboardText);
            } else {
                Toast.makeText(this, "Скопируйте свои оценки, чтобы вставить их в" +
                        " калькулятор", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Скопируйте свои оценки, чтобы вставить их в" +
                    " калькулятор", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseAndAddGrades(String text) {
        String numbersOnly = text.replaceAll("[^0-9]", "");

        if (numbersOnly.isEmpty()) {
            Toast.makeText(this, "Скопируйте свои оценки, чтобы вставить их в\" +\n" +
                    "                        \" калькулятор", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> newGrades = new ArrayList<>();
        for (int i = 0; i < numbersOnly.length(); i++) {
            char c = numbersOnly.charAt(i);
            int digit = Character.getNumericValue(c);

            if (digit >= 2 && digit <= 5) {
                newGrades.add(digit);
            }
        }

        if (!newGrades.isEmpty()) {
            rating.addAll(newGrades);
            updateText();
        }
    }

    private int calculateNeededWithSpecificGrade(int sum, int count, float target, int grade) {
        float needed = (target * count - sum) / (grade - target);
        int result = (int) Math.ceil(needed);
        return (result < 0 || Float.isInfinite(needed) || Float.isNaN(needed)) ? 0 : result;
    }

    private String getGradeWord(int count, int grade) {
        if (count == 0) return "";

        String[] words;
        switch (grade) {
            case 5:
                words = new String[]{"пятёрку", "пятёрки", "пятёрок"};
                break;
            case 4:
                words = new String[]{"четвёрку", "четвёрки", "четвёрок"};
                break;
            case 3:
                words = new String[]{"тройку", "тройки", "троек"};
                break;
            case 2:
                words = new String[]{"двойку", "двойки", "двоек"};
                break;
            default:
                return "";
        }

        if (count == 1) return words[0];
        if (count >= 2 && count <= 4) return words[1];
        return words[2];
    }

    private void btnAnim(View v) {
        v.animate()
                .setDuration(100)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .withEndAction(() -> {
                    v.animate()
                            .setDuration(100)
                            .scaleX(1f)
                            .scaleY(1f)
                            .start();
                })
                .start();
    }

    private void onPressedBack() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });
    }
}
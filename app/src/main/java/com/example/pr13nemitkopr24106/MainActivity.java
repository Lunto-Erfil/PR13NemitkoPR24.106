package com.example.pr13nemitkopr24106;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    // Флажки состояния игры
    private boolean isStarted = false;
    private boolean isFinished = false;

    private boolean isTwoPlayerMode = false;


    private float carBlueOffset = 0f;
    private float carGreenOffset = 0f;
    private float moveStepPx;
    private float startMarginPx;
    private float finishLineStartX;

    private ImageView carBlue;
    private ImageView carGreen;
    private ImageView finishLine;
    private Button btnStart;
    private Button btnRide1;
    private Button btnRide2;
    private TextView textResult;
    private TextView btnModeSwitch;

    private Handler aiHandler;
    private Runnable aiRunnable;

    private static final float MOVE_STEP_DP = 25f;
    private static final float START_MARGIN_DP = 25f;
    private static final int AI_MOVE_DELAY_MS = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } catch (NullPointerException ignored) {
        }
        setContentView(R.layout.activity_main);

        float density = getResources().getDisplayMetrics().density;
        moveStepPx = MOVE_STEP_DP * density;
        startMarginPx = START_MARGIN_DP * density;

        aiHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupAiRunnable();
    }

    private void initViews() {
        carBlue = findViewById(R.id.carBlue);
        carGreen = findViewById(R.id.carGreen);
        finishLine = findViewById(R.id.finishLine);
        btnStart = findViewById(R.id.btnStart);
        btnRide1 = findViewById(R.id.btnRide1);
        btnRide2 = findViewById(R.id.btnRide2);
        textResult = findViewById(R.id.textResult);
        btnModeSwitch = findViewById(R.id.btnModeSwitch);

        if (btnModeSwitch != null) {
            btnModeSwitch.setOnClickListener(v -> toggleGameMode());
        }

        if (textResult != null) {
            textResult.setText("");
        }

        updateModeUi();

        if (finishLine != null) {
            finishLine.post(() -> finishLineStartX = finishLine.getX());
        }

        resetGame();
    }

    private void toggleGameMode() {
        if (isStarted) {
            return;
        }
        isTwoPlayerMode = !isTwoPlayerMode;
        updateModeUi();
        resetGame();
    }

    @SuppressLint("SetTextI18n")
    private void updateModeUi() {
        if (btnModeSwitch != null) {
            btnModeSwitch.setText(isTwoPlayerMode
                    ? "Режим: 2 игрока (нажмите)"
                    : "Режим: 1 игрок (нажмите)");
        }
        if (btnRide1 != null) {
            btnRide1.setVisibility(isTwoPlayerMode ? View.VISIBLE : View.GONE);
        }
    }

    private void setupAiRunnable() {
        aiRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isTwoPlayerMode && isStarted && !isFinished) {
                    moveCar(carBlue, true);
                    aiHandler.postDelayed(this, AI_MOVE_DELAY_MS);
                }
            }
        };
    }

    @SuppressLint("SetTextI18n")
    public void Start(View view) {
        if (isFinished) {
            resetGame();
            return;
        }

        if (!isStarted) {
            isStarted = true;
            btnStart.setText("Пауза");
            if (textResult != null) {
                textResult.setText("");
            }
            if (!isTwoPlayerMode) {
                aiHandler.post(aiRunnable);
            }
        } else {
            isStarted = false;
            btnStart.setText("Старт");
            if (!isTwoPlayerMode) {
                aiHandler.removeCallbacks(aiRunnable);
            }
        }
    }

    public void Ride1(View view) {
        if (isStarted && !isFinished && isTwoPlayerMode) {
            moveCar(carBlue, true);
        }
    }

    public void Ride2(View view) {
        if (isStarted && !isFinished) {
            moveCar(carGreen, false);
        }
    }

    private void moveCar(ImageView car, boolean isBlueCar) {
        if (car == null || isFinished) {
            return;
        }

        float currentOffset = isBlueCar ? carBlueOffset : carGreenOffset;
        float newOffset = currentOffset + moveStepPx;

        if (isBlueCar) {
            carBlueOffset = newOffset;
        } else {
            carGreenOffset = newOffset;
        }

        car.setTranslationX(newOffset);

        float finishX = finishLineStartX > 0f
                ? finishLineStartX
                : (finishLine != null ? finishLine.getX()
                : getResources().getDisplayMetrics().widthPixels - 100f);
        float carRightEdge = car.getX() + car.getWidth();

        if (carRightEdge >= finishX) {
            finishRace(isBlueCar);
        }
    }

    @SuppressLint("SetTextI18n")
    private void finishRace(boolean blueCarWon) {
        isFinished = true;
        isStarted = false;
        aiHandler.removeCallbacks(aiRunnable);

        if (textResult != null) {
            textResult.setText(blueCarWon
                    ? "Победила синяя машина!"
                    : "Победила зелёная машина!");
        }
        btnStart.setText("Заново");
    }

    private void resetGame() {
        isStarted = false;
        isFinished = false;
        aiHandler.removeCallbacks(aiRunnable);

        carBlueOffset = 0f;
        carGreenOffset = 0f;

        resetCarPosition(carBlue);
        resetCarPosition(carGreen);

        if (textResult != null) {
            textResult.setText("");
        }
        btnStart.setText("Старт");
    }

    private void resetCarPosition(ImageView car) {
        if (car == null) {
            return;
        }
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) car.getLayoutParams();
        int margin = (int) startMarginPx;
        params.leftMargin = margin;
        params.setMarginStart(margin);
        car.setLayoutParams(params);
        car.setTranslationX(0f);
    }
}

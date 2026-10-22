package com.a.wwww;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView expressionView;
    private TextView resultView;

    private String expression = "";
    private String result = "0";

    private final int BLACK = Color.BLACK;
    private final int WHITE = Color.WHITE;
    private final int ORANGE = Color.rgb(255, 159, 10);
    private final int DARK_GRAY = Color.rgb(51, 51, 51);
    private final int LIGHT_GRAY = Color.rgb(165, 165, 165);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BLACK);
        getWindow().setNavigationBarColor(BLACK);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BLACK);
        root.setPadding(dp(10), dp(10), dp(10), dp(10));

        expressionView = new TextView(this);
        expressionView.setText("");
        expressionView.setTextColor(Color.LTGRAY);
        expressionView.setTextSize(30);
        expressionView.setGravity(
                Gravity.END | Gravity.CENTER_VERTICAL
        );
        expressionView.setMaxLines(2);

        resultView = new TextView(this);
        resultView.setText("0");
        resultView.setTextColor(WHITE);
        resultView.setTextSize(68);
        resultView.setGravity(
                Gravity.END | Gravity.CENTER_VERTICAL
        );
        resultView.setTypeface(
                Typeface.create("sans-serif-light", Typeface.NORMAL)
        );
        resultView.setSingleLine(true);

        root.addView(expressionView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        ));

        root.addView(resultView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.2f
        ));

        String[][] rows = {
                {"AC", "+/-", "%", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "-"},
                {"1", "2", "3", "+"},
                {"0", ".", "="}
        };

        for (String[] row : rows) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);

            for (String label : row) {
                TextView button = createButton(label);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                0, 0, label.equals("0") ? 2.0f : 1.0f
                        );

                params.setMargins(dp(4), dp(4), dp(4), dp(4));

                rowLayout.addView(button, params);
            }

            root.addView(rowLayout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
            ));
        }

        setContentView(root);
    }

    private TextView createButton(String label) {
        TextView button = new TextView(this);

        int backgroundColor;
        int textColor;

        if (label.equals("÷") || label.equals("×")
                || label.equals("-") || label.equals("+")
                || label.equals("=")) {
            backgroundColor = ORANGE;
            textColor = WHITE;
        } else if (label.equals("AC") || label.equals("+/-")
                || label.equals("%")) {
            backgroundColor = LIGHT_GRAY;
            textColor = BLACK;
        } else {
            backgroundColor = DARK_GRAY;
            textColor = WHITE;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(backgroundColor);

        button.setBackground(shape);
        button.setText(label);
        button.setTextColor(textColor);
        button.setTextSize(27);
        button.setGravity(Gravity.CENTER);
        button.setClickable(true);
        button.setFocusable(true);

        button.setOnClickListener(v -> handleInput(label));

        return button;
    }

    private void handleInput(String value) {
        switch (value) {
            case "AC":
                expression = "";
                result = "0";
                break;

            case "+/-":
                try {
                    double number = Double.parseDouble(result);
                    result = formatNumber(-number);
                    expression = result;
                } catch (Exception ignored) {
                }
                break;

            case "%":
                try {
                    double number = Double.parseDouble(result);
                    result = formatNumber(number / 100.0);
                    expression = result;
                } catch (Exception ignored) {
                }
                break;

            case "=":
                calculate();
                break;

            default:
                if (isOperator(value)) {
                    if (expression.isEmpty() && !value.equals("-")) {
                        return;
                    }

                    if (!expression.isEmpty()) {
                        char last = expression.charAt(
                                expression.length() - 1
                        );

                        if (isOperator(String.valueOf(last))) {
                            expression = expression.substring(
                                    0, expression.length() - 1
                            );
                        }
                    }
                }

                if (value.equals(".")) {
                    String current = getCurrentNumber(expression);
                    if (current.contains(".")) {
                        return;
                    }
                }

                expression += value;
                result = expression;
                break;
        }

        updateDisplay();
    }

    private boolean isOperator(String value) {
        return value.equals("+") || value.equals("-")
                || value.equals("×") || value.equals("÷");
    }

    private String getCurrentNumber(String input) {
        int index = input.length() - 1;

        while (index >= 0) {
            char c = input.charAt(index);
            if (c == '+' || c == '-' || c == '×' || c == '÷') {
                break;
            }
            index--;
        }

        return input.substring(index + 1);
    }

    private void calculate() {
        if (expression.isEmpty()) {
            return;
        }

        try {
            double answer = evaluateExpression(expression);
            result = formatNumber(answer);
            expression = result;
        } catch (Exception e) {
            result = "خطأ";
        }

        updateDisplay();
    }

    private double evaluateExpression(String input) {
        List<Double> numbers = new ArrayList<>();
        List<Character> operators = new ArrayList<>();

        int i = 0;
        boolean expectNumber = true;

        while (i < input.length()) {
            char c = input.charAt(i);

            if (c == ' ') {
                i++;
                continue;
            }

            if (expectNumber) {
                int start = i;

                if (c == '-') {
                    i++;
                }

                boolean hasDigit = false;
                boolean hasDot = false;

                while (i < input.length()) {
                    char n = input.charAt(i);

                    if (Character.isDigit(n)) {
                        hasDigit = true;
                        i++;
                    } else if (n == '.' && !hasDot) {
                        hasDot = true;
                        i++;
                    } else {
                        break;
                    }
                }

                if (!hasDigit) {
                    throw new IllegalArgumentException();
                }

                numbers.add(Double.parseDouble(
                        input.substring(start, i)
                ));

                expectNumber = false;
            } else {
                if (c != '+' && c != '-' && c != '×' && c != '÷') {
                    throw new IllegalArgumentException();
                }

                operators.add(c);
                i++;
                expectNumber = true;
            }
        }

        if (expectNumber || numbers.size() != operators.size() + 1) {
            throw new IllegalArgumentException();
        }

        // تنفيذ الضرب والقسمة أولاً
        i = 0;

        while (i < operators.size()) {
            char op = operators.get(i);

            if (op == '×' || op == '÷') {
                double a = numbers.get(i);
                double b = numbers.get(i + 1);

                if (op == '÷' && b == 0.0) {
                    throw new ArithmeticException();
                }

                double value = op == '×' ? a * b : a / b;

                numbers.set(i, value);
                numbers.remove(i + 1);
                operators.remove(i);
            } else {
                i++;
            }
        }

        // تنفيذ الجمع والطرح
        double answer = numbers.get(0);

        for (i = 0; i < operators.size(); i++) {
            char op = operators.get(i);
            double next = numbers.get(i + 1);

            if (op == '+') {
                answer += next;
            } else {
                answer -= next;
            }
        }

        if (Double.isNaN(answer) || Double.isInfinite(answer)) {
            throw new ArithmeticException();
        }

        return answer;
    }

    private String formatNumber(double number) {
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            return "خطأ";
        }

        return BigDecimal.valueOf(number)
                .stripTrailingZeros()
                .toPlainString();
    }

    private void updateDisplay() {
        expressionView.setText(expression);
        resultView.setText(result);
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }
}

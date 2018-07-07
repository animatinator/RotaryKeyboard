package com.animatinator.rotarykeyboard;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.animatinator.rotarykeyboard.keyboard.RotaryKeyboardView;

public class RotaryKeyboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotary_keyboard);
        final TextView textView = findViewById(R.id.enteredText);
        RotaryKeyboardView keyboardView = findViewById(R.id.keyboard);
        keyboardView.setLetters(new String[] {"c", "a", "u", "s", "e", "d"});
        keyboardView.setWordEntryCallback(new RotaryKeyboardView.WordEntryCallback() {
            @Override
            public void onWordEntered(String word) {
                textView.setTextColor(Color.BLACK);
                textView.setText(word);
            }

            @Override
            public void onPartialWord(String partialWord) {
                textView.setTextColor(Color.GRAY);
                textView.setText(partialWord);
            }
        });
    }
}

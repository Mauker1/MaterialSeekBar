package br.com.mauker.materialseekbar.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.com.mauker.materialseekbar.ColorSeekBar;

public class MainActivity extends AppCompatActivity {

    private ColorSeekBar sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sb = (ColorSeekBar) findViewById(R.id.sb);
    }
}

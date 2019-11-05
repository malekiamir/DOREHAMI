package ir.ac.kntu.patogh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainActivity extends AppCompatActivity {

    final int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Glide.with(this.getApplicationContext())
                .load(R.drawable.back)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(7, 3)))
                .into((ImageView) findViewById(R.id.bg));
    }

    public void clickHandler(View view) {
        if(view.getId() == R.id.btn_signup) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, view, "transition");

            int revealX = (int) (view.getX() + view.getWidth() / 2);
            int revealY = (int) (view.getY() + view.getHeight() / 2);
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            intent.putExtra(SignUpActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
            intent.putExtra(SignUpActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);

            ActivityCompat.startActivity(this, intent, options.toBundle());
        }
    }
}

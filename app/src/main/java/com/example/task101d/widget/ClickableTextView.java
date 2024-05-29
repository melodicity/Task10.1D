package com.example.task101d.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.example.task101d.R;

public class ClickableTextView extends AppCompatTextView {
    private Rect mRect; // Stores the bounds of the view

    public ClickableTextView(Context context) {
        super(context);
        init();
    }

    public ClickableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRect = new Rect();
        setTextColor(ContextCompat.getColor(getContext(), R.color.primary20));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Change the view colour when touched
                setTextColor(ContextCompat.getColor(getContext(), R.color.primary10));
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Reset the view colour when released
                setTextColor(ContextCompat.getColor(getContext(), R.color.primary20));

                // Check if the touch event is within the bounds of the view
                if (isTouchInsideView(event)) {
                    // If so, the text was clicked - call performClick()
                    performClick();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    // Helper method to check if touch event occurred inside the view bounds
    private boolean isTouchInsideView(MotionEvent event) {
        mRect.set(0, 0, getWidth(), getHeight()); // Set the bounds of the view
        return mRect.contains((int) event.getX(), (int) event.getY()); // Check if touch event is within bounds
    }
}
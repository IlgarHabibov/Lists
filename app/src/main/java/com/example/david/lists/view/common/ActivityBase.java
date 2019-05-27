package com.example.david.lists.view.common;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import javax.inject.Inject;

public abstract class ActivityBase extends AppCompatActivity {
    @Inject
    protected FragmentManager fragmentManager;

    // Used when adding Fragments to the layout.
    // If this Activity is being recreated, do not add the Fragment
    // to the layout - the Activity will handle restoration for you.
    protected boolean newActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.newActivity = (savedInstanceState == null);
    }

    protected void addFragment(Fragment fragment, int containerViewId) {
        fragmentManager.beginTransaction()
                .add(containerViewId, fragment)
                .commit();
    }
}

package de.igelstudios.substitution;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import de.igelstudios.substitution.databinding.FragmentLogviewBinding;

public class FragmentLogview extends Fragment {
    public FragmentLogviewBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentLogviewBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TextView) MainActivity.getInstance().findViewById(R.id.log_view)).setText(Logger.get().read());
    }
}

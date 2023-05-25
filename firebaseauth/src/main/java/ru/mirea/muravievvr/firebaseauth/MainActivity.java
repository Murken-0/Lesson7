package ru.mirea.muravievvr.firebaseauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import ru.mirea.muravievvr.firebaseauth.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        binding.emailCreateAccountButton.setOnClickListener(v ->
                createAccount(binding.fieldEmail.getText().toString(),
                        binding.fieldPassword.getText().toString()));

        binding.signOutButton.setOnClickListener(v -> signOut());

        binding.emailSignInButton.setOnClickListener(v ->
                signIn(binding.fieldEmail.getText().toString(),
                        binding.fieldPassword.getText().toString()));

        binding.verButton.setOnClickListener(v -> {
            sendEmailVerification();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            binding.status.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            binding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            binding.emailPasswordButtons.setVisibility(View.GONE);
            binding.emailPasswordFields.setVisibility(View.GONE);
            binding.signOutButton.setVisibility(View.VISIBLE);
            binding.verButton.setVisibility(View.VISIBLE);
            binding.verButton.setEnabled(!user.isEmailVerified());
            binding.signOutButton.setVisibility(View.VISIBLE);
        }
        else {
            binding.status.setText(R.string.signed_out);
            binding.detail.setText(null);
            binding.emailPasswordButtons.setVisibility(View.VISIBLE);
            binding.emailPasswordFields.setVisibility(View.VISIBLE);
            binding.signOutButton.setVisibility(View.GONE);
            binding.verButton.setVisibility(View.GONE);
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "createUserWithEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                Log.w(TAG, "createUserWithEmail:failure");
                Toast.makeText(MainActivity.this, "Authentication Failed.",Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            }
            else {
                Toast.makeText(MainActivity.this, "Authentication Failed",
                        Toast.LENGTH_SHORT).show(); updateUI(null);
            }
            if (!task.isSuccessful()) {
                binding.status.setText(R.string.auth_failed); }
        });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification() {
        final FirebaseUser user = mAuth.getCurrentUser();
        Objects.requireNonNull(user).sendEmailVerification().addOnCompleteListener(this, task -> {
            binding.verButton.setEnabled(true);
            if (task.isSuccessful()) { Toast.makeText(MainActivity.this,
                    "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "sendEmailVerification", task.getException());
                Toast.makeText(MainActivity.this,
                        "Failed to send verification email.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.wap;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.assertion.ViewAssertions;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Authorization;
import com.example.wap.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.junit.Test;

import static org.junit.Assert.*;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class SignUpActivityTest {
    final String email_user = "test@email.com";
    final String password_user = "testing";
    final String email_admin = "test@admin.com";
    final String password_admin = "testadmin";

    @Test
    public void test_correctview() {
        ActivityScenario activityScenario = ActivityScenario.launch(SignUpActivity.class);
        onView(withId(R.id.etEmailSignup)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.etUsernameSignup)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.etPasswordSignup)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.authSpinner)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.UserType)).check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void test_create_user() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        User currentUser = new User();
        currentUser.setEmail(email_user);
        currentUser.setUsername(email_user);
        currentUser.setAuth(Authorization.USER);

        WAPFirebase<User> userWAPFirebase = new WAPFirebase<>(User.class, "users");
        boolean inputsValid = AuthUtils.validateEmail(email_user) && AuthUtils.validateInputField(password_user, "password");
        if (inputsValid) {
            firebaseAuth.createUserWithEmailAndPassword(email_user, password_user).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    userWAPFirebase.create(currentUser, firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            userWAPFirebase.delete(firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    assert true;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    fail();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fail();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        assert true;
                    } else fail();
                }
            });
            ;
        } else {
            fail();
        }
    }

    @Test
    public void test_create_admin() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        User currentUser = new User();
        currentUser.setEmail(email_admin);
        currentUser.setUsername(email_admin);
        currentUser.setAuth(Authorization.ADMIN);

        WAPFirebase<User> userWAPFirebase = new WAPFirebase<>(User.class, "users");
        boolean inputsValid = AuthUtils.validateEmail(email_admin) && AuthUtils.validateInputField(password_admin, "password");
        if (inputsValid) {
            firebaseAuth.createUserWithEmailAndPassword(email_admin, password_admin).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    userWAPFirebase.create(currentUser, firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            userWAPFirebase.delete(firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    assert true;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    fail();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fail();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        assert true;
                    } else fail();
                }
            });
            ;
        } else {
            fail();
        }
    }

   /* @Test
    public void duplicate_Email() {
        ActivityScenario activityScenario = ActivityScenario.launch(SignUpActivity.class);
        onView(withId(R.id.etEmailSignup)).perform(click(), typeText("wes0903@gmail.com"));
        onView(withId(R.id.etUsernameSignup)).perform(click(),typeText("wes"));
        onView(withId(R.id.etPasswordSignup)).perform(click(),typeText("123456"));
//        onView(withId(R.id.signupButton)).perform(click());
        onView(withId(R.id.loginActivity)).check(ViewAssertions.doesNotExist());
    }*/
}
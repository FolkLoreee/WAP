package com.example.wap;

import androidx.annotation.NonNull;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Authorization;
import com.example.wap.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

;

@RunWith(AndroidJUnit4.class)


public class LoginActivityTest extends TestCase {
//! Instrumented Tests

    final String email_admin = "wapsutd@gmail.com";
    final String password_admin = "wapwap";
    final String email_user = "cia.filbert@gmail.com";
    final String password_user = "filbert";

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void test_instantiating_firebase_auth() {
        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        assertNotNull(fbAuth);
    }

    @Test
    public void test_query_user_list() {
        String uid = "wf1AYCqQpkbLXp5mZJlpavPzefu1";
        WAPFirebase<User> userWAPFirebase = new WAPFirebase<>(User.class, "users");
        userWAPFirebase.query(uid).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                assertNotNull(user);
            }
        });
    }
    @Test
    public void test_login_admin(){
        String uid = "wf1AYCqQpkbLXp5mZJlpavPzefu1";
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        WAPFirebase<User> userWAPFirebase = new WAPFirebase<>(User.class, "users");

        boolean inputsValid = AuthUtils.validateEmail(email_admin) && AuthUtils.validateInputField(password_admin, "password");
        if (inputsValid) {
            firebaseAuth.signInWithEmailAndPassword(email_admin, password_admin).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    userWAPFirebase.query(firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            Authorization userAuth = user.getAuth();
                            assertEquals(userAuth, Authorization.ADMIN);
                            assertEquals(uid,firebaseAuth.getCurrentUser().getUid());
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

    }
    @Test
    public void test_login_user() {
        String uid = "TvIZYpR9eycp5FMggaKq5UDwwCY2";
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        WAPFirebase<User> userWAPFirebase = new WAPFirebase<>(User.class, "users");

        boolean inputsValid = AuthUtils.validateEmail(email_user) && AuthUtils.validateInputField(password_user, "password");
        if (inputsValid) {
            firebaseAuth.signInWithEmailAndPassword(email_user, password_user).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    userWAPFirebase.query(firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            Authorization userAuth = user.getAuth();
                            assertEquals(userAuth, Authorization.USER);
                            assertEquals(uid, firebaseAuth.getCurrentUser().getUid());
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
    }


    //? UI Tests
    @Test
    public void test_IsLoginInView() {
        onView(withId(R.id.loginActivity)).check(matches(isDisplayed()));
    }

    @Test
    public void test_IsItemsDisplayed() {
        onView(withId(R.id.applogo)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.passwordfield)).check(matches(isDisplayed()));
        onView(withId(R.id.loginfield)).check(matches(isDisplayed()));
        onView(withId(R.id.textView3)).check(matches(isDisplayed()));
        onView(withId(R.id.textView4)).check(matches(isDisplayed()));
    }

    @Test
    public void test_valid_credential_success() {
        onView(withId(R.id.loginfield)).perform(click(), typeText(email_admin), closeSoftKeyboard());
        onView(withId(R.id.passwordfield)).perform(click(), typeText(password_admin), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
    }

    //
    @Test
    public void test_navMainActivityFailId() {
        onView(withId(R.id.loginfield)).perform(click(), typeText("Hi prof"), closeSoftKeyboard());
        onView(withId(R.id.passwordfield)).perform(click(), typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        onView(withId(R.id.choosemapactivity)).check(ViewAssertions.doesNotExist());
    }
//    }
//    @Test
//    public void test_navMainActivityFailPw() {
//        ActivityScenario activityscenario = ActivityScenario.launch(LoginActivity.class);
//        onView(withId(R.id.loginfield)).perform(click(), typeText("admin"),closeSoftKeyboard());
//        onView(withId(R.id.passwordfield)).perform(click(), typeText("ESC rules"),closeSoftKeyboard());
//        onView(withId(R.id.login_button)).perform(click());
//
//        onView(withId(R.id.mainActivity)).check(ViewAssertions.doesNotExist());
//    }

}
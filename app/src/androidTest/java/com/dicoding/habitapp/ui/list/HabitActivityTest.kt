package com.dicoding.habitapp.ui.list

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import com.dicoding.habitapp.R
import com.dicoding.habitapp.ui.add.AddHabitActivity
import org.junit.After
import org.junit.Before
import org.junit.Test

//TODO 16 : Write UI test to validate when user tap Add Habit (+), the AddHabitActivity displayed
class HabitActivityTest {
    @Before
    fun setup() {
        ActivityScenario.launch(HabitListActivity::class.java)
        Intents.init()
    }

    @After
    fun destroy() {
        Intents.release()
    }

    @Test
    fun shouldDisplayAddCourseActivity() {
        onView(ViewMatchers.withId(R.id.fab)).perform(click())
        intended(hasComponent(AddHabitActivity::class.java.name))
    }
}
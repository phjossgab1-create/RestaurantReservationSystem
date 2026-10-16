package ui;

import exception.GoBackException;

import java.util.Arrays;
import java.util.List;

/**
 * Runs a series of questions (steps) one after another.
 *
 * Typing \ on a question goes back ONE question, so the user can fix the previous answer.
 * Typing \ on the first question leaves the form and returns to the menu it came from.
 *
 * Each step is a small lambda that asks one question and stores the answer.
 */
public final class StepForm {

    /** One question in the form. */
    @FunctionalInterface
    public interface Step {
        void ask();
    }

    private StepForm() {
    }

    public static void run(Step... steps) {
        run(Arrays.asList(steps));
    }

    public static void run(List<Step> steps) {
        int index = 0;
        while (index < steps.size()) {
            try {
                steps.get(index).ask();
                index++;
            } catch (GoBackException e) {
                if (index == 0) {
                    throw e; // back from the first question -> back to the menu
                }
                index--;     // back to the previous question
                System.out.println("  (Back to the previous question)");
            }
        }
    }
}

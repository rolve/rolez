package rolez.lang;

import static java.lang.Integer.toHexString;
import static java.lang.System.identityHashCode;
import static rolez.lang.Task.idBitsFor;
import static rolez.lang.Task.idForBits;

public class InterferenceException extends RuntimeException {

    private static String formatObject(Guarded object) {
        String toString = object.toString();
        String rawToString = object.getClass().getName() + "@" + toHexString(identityHashCode(object));
        return toString.equals(rawToString) ? toString : toString + " (" + rawToString + ")";
    }

    private static String formatMessage(Guarded object, Guarded view, String newRole, String interferingRole,
            long newTaskBits, long interferingTasks) {
        String tasks;
        if(interferingRole.equals("readwrite")) {
            tasks = "task " + idForBits(interferingTasks);
        }
        else {
            assert interferingRole.equals("readonly");
            tasks = "tasks ";
            for(int i = 0; i < 64; i++)
                if((idBitsFor(i) & interferingTasks) != 0)
                    tasks += i + ", ";
            tasks = tasks.substring(0, tasks.length() - 2);
        }
        String viewPart = "";
        if(object != view) {
            viewPart = "overlapping slice " + formatObject(view) + " ";
        }
        return "cannot " + newRole + "-share object " + formatObject(object) + " with task " +
                idForBits(newTaskBits) + ", " + viewPart + "already " + interferingRole + "-shared with " + tasks;
    }

    public InterferenceException(Guarded object, Guarded view, String newRole, String interferingRole,
            long newTaskBits, long interferingTasks) {
        this(formatMessage(object, view, newRole, interferingRole, newTaskBits, interferingTasks));
    }

    public InterferenceException(String message) {
        super(message);
    }
}
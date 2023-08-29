package opt.api.actions;

import java.util.Arrays;

public enum Action {
    GET, POST, PUT, DELETE, PATCH, OPTIONS;

    public static String getCommaSeparatedString() {
        return String.join(", ", Arrays.stream(Action.values())
                                       .map(Enum::name)
                                       .toArray(String[]::new));
    }
}

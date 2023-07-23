package opt.api.routing;

public enum Route {
    INTERPRET("interpret");

    private final String routeName;

    Route(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteName() {
        return routeName;
    }

    // Static method to get the enum value from a string
    public static Route fromString(String text) {
        for (Route myEnum : Route.values()) {
            if (myEnum.routeName.equals(text)) {
                return myEnum;
            }
        }
        throw new IllegalArgumentException("No enum constant with text: " + text);
    }
}

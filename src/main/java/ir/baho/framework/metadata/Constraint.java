package ir.baho.framework.metadata;

public enum Constraint {

    IS_NULL,
    IS_NOT_NULL,
    EQUALS,
    NOT_EQUALS,
    EQUALS_IGNORE_CASE,
    NOT_EQUALS_IGNORE_CASE,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN_OR_EQUALS,
    CONTAINS,
    NOT_CONTAINS,
    STARTS_WITH,
    NOT_STARTS_WITH,
    ENDS_WITH,
    NOT_ENDS_WITH,
    CONTAINS_IGNORE_CASE,
    NOT_CONTAINS_IGNORE_CASE,
    STARTS_WITH_IGNORE_CASE,
    NOT_STARTS_WITH_IGNORE_CASE,
    ENDS_WITH_IGNORE_CASE,
    NOT_ENDS_WITH_IGNORE_CASE,
    BETWEEN,
    NOT_BETWEEN,
    IN,
    NOT_IN,
    UNKNOWN;

    public static Constraint of(String value) {
        return switch (value) {
            case "null" -> IS_NULL;
            case "!null" -> IS_NOT_NULL;
            case "eq" -> EQUALS;
            case "ne" -> NOT_EQUALS;
            case "ieq" -> EQUALS_IGNORE_CASE;
            case "ine" -> NOT_EQUALS_IGNORE_CASE;
            case "gt" -> GREATER_THAN;
            case "lt" -> LESS_THAN;
            case "ge" -> GREATER_THAN_OR_EQUALS;
            case "le" -> LESS_THAN_OR_EQUALS;
            case "contains" -> CONTAINS;
            case "!contains" -> NOT_CONTAINS;
            case "starts" -> STARTS_WITH;
            case "!starts" -> NOT_STARTS_WITH;
            case "ends" -> ENDS_WITH;
            case "!ends" -> NOT_ENDS_WITH;
            case "icontains" -> CONTAINS_IGNORE_CASE;
            case "!icontains" -> NOT_CONTAINS_IGNORE_CASE;
            case "istarts" -> STARTS_WITH_IGNORE_CASE;
            case "!istarts" -> NOT_STARTS_WITH_IGNORE_CASE;
            case "iends" -> ENDS_WITH_IGNORE_CASE;
            case "!iends" -> NOT_ENDS_WITH_IGNORE_CASE;
            case "between" -> BETWEEN;
            case "!between" -> NOT_BETWEEN;
            case "in" -> IN;
            case "!in" -> NOT_IN;
            default -> UNKNOWN;
        };
    }

    public static Constraint[] all() {
        return new Constraint[]{IS_NULL, IS_NOT_NULL, EQUALS, NOT_EQUALS, EQUALS_IGNORE_CASE, NOT_EQUALS_IGNORE_CASE,
                GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS,
                CONTAINS, NOT_CONTAINS, STARTS_WITH, NOT_STARTS_WITH, ENDS_WITH, NOT_ENDS_WITH,
                CONTAINS_IGNORE_CASE, NOT_CONTAINS_IGNORE_CASE,
                STARTS_WITH_IGNORE_CASE, NOT_STARTS_WITH_IGNORE_CASE, ENDS_WITH_IGNORE_CASE, NOT_ENDS_WITH_IGNORE_CASE,
                BETWEEN, NOT_BETWEEN, IN, NOT_IN};
    }

    public static Constraint[] stringOnly() {
        return new Constraint[]{CONTAINS, NOT_CONTAINS,
                STARTS_WITH, NOT_STARTS_WITH,
                ENDS_WITH, NOT_ENDS_WITH,
                CONTAINS_IGNORE_CASE, NOT_CONTAINS_IGNORE_CASE,
                STARTS_WITH_IGNORE_CASE, NOT_STARTS_WITH_IGNORE_CASE,
                ENDS_WITH_IGNORE_CASE, NOT_ENDS_WITH_IGNORE_CASE};
    }

}

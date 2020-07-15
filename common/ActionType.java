package common;

import java.io.Serializable;

public enum ActionType implements Serializable {
    SEND,
    DELETE,
    UNKNOWN_RECIPIENT,
    SUCCESSFUL_OP
}

package tournManager;

import java.io.Serializable;

public enum WinStatus implements Serializable {
    PENDING,
    PLAYER1,
    PLAYER2,
    CONFLICT
}

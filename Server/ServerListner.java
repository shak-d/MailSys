package Server;

public interface ServerListner {

    void onServerEvent(ServerLog event, Object ...args);
    void onServerError(ServerError error, Object ...args);
}

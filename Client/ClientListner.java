package Client;

public interface ClientListner {

    void onClientError(ClientLog error, Object ...param);
    void onServerResponse(ClientLog event, Object ...param);
}

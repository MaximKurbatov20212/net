package nsu.maxwell.attachment;

public enum KeyState {
    ACCEPT,
    INIT_REQUEST,
    INIT_RESPONSE_SUCCESS,
    INIT_RESPONSE_FAILED,
    CONNECT_REQUEST,
    CONNECT_RESPONSE_SUCCESS,
    CONNECT_RESPONSE_FAILED,
    CONNECT_RESPONSE_UNAVAILABLE,
    DNS_RESPONSE,
    FINISH_CONNECT,
    PROXYING
}

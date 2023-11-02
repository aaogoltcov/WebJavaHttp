public class Handler {
    final String method;
    final String url;
    final String responseText;

    public Handler(String method, String url, String responseText) {
        this.method = method;
        this.url = url;
        this.responseText = responseText;
    }
}

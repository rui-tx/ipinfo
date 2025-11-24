package io.github.ruitx;

import java.util.List;
import java.util.Map;

public record ClientInfo(
    String ip,                    // Real public IP (smart detection)
    Integer port,                 // Client source port
    String method,
    String scheme,                // http or https (you can detect via SSL handler or header)
    String host,
    String path,
    String queryString,
    String uri,
    String userAgent,
    String acceptLanguage,
    String acceptEncoding,
    String origin,
    String referer,
    String cfConnectingIp,        // Cloudflare
    String xForwardedFor,         // Raw header
    String xRealIp,
    Map<String, String> headers,  // All headers (lowercase keys)
    Map<String, List<String>> queryParams,
    String body) {

}

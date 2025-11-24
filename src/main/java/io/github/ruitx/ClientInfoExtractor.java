package io.github.ruitx;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.nexus.RequestContext;

public final class ClientInfoExtractor {

  private ClientInfoExtractor() {
  }

  public static ClientInfo fromRequestContext(RequestContext ctx) {
    var request = ctx.getRequest();
    var headers = request.headers();
    var uri = request.uri();

    // Smart IP detection (Cloudflare → X-Forwarded-For → X-Real-IP → remote addr)
    String realIp = extractRealClientIp(ctx, headers);

    int clientPort = -1;
    if (ctx.getCtx().channel().remoteAddress() instanceof InetSocketAddress isa) {
      clientPort = isa.getPort();
    }

    // Build lowercase header map for easy access
    Map<String, String> headerMap = new HashMap<>();
    for (Map.Entry<String, String> h : headers) {
      headerMap.put(h.getKey().toLowerCase(), h.getValue());
    }

    // Query string (after ?)
    String queryString = uri.indexOf('?') >= 0 ? uri.substring(uri.indexOf('?') + 1) : null;

    return new ClientInfo(
        realIp,
        clientPort > 0 ? clientPort : null,
        request.method().name(),
        "http", // change to "https" if you detect SSL via SslHandler on channel
        headers.get(HttpHeaderNames.HOST),
        new io.netty.handler.codec.http.QueryStringDecoder(uri).path(),
        queryString,
        uri,
        headers.get(HttpHeaderNames.USER_AGENT),
        headers.get(HttpHeaderNames.ACCEPT_LANGUAGE),
        headers.get(HttpHeaderNames.ACCEPT_ENCODING),
        headers.get(HttpHeaderNames.ORIGIN),
        headers.get(HttpHeaderNames.REFERER),
        headers.get("CF-Connecting-IP"),
        headers.get("X-Forwarded-For"),
        headers.get("X-Real-IP"),
        Map.copyOf(headerMap),
        Map.copyOf(ctx.getQueryParams()),
        ctx.getBody()
    );
  }

  private static String extractRealClientIp(RequestContext ctx, HttpHeaders headers) {
    // 1. Cloudflare
    String cf = headers.get("CF-Connecting-IP");
    if (cf != null && !cf.isBlank()) {
      return cf.trim();
    }

    // 2. X-Forwarded-For — take the first (original client)
    String xff = headers.get("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      String[] parts = xff.split(",");
      return parts[0].trim();
    }

    // 3. X-Real-IP
    String realIp = headers.get("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }

    // 4. Direct connection
    if (ctx.getCtx().channel().remoteAddress() instanceof InetSocketAddress isa) {
      return isa.getAddress().getHostAddress();
    }

    return "unknown";
  }
}

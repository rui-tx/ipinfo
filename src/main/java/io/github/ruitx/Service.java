package io.github.ruitx;

import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.nexus.NexusExecutor;
import org.nexus.RequestContext;
import org.nexus.Response;
import org.nexus.enums.ProblemDetailsTypes;
import org.nexus.exceptions.ProblemDetailsException;
import org.nexus.interfaces.ProblemDetails;

@Singleton
public class Service {

  public CompletableFuture<Response<ClientInfo>> ipReport(RequestContext rc) {
    return CompletableFuture.supplyAsync(
        () -> ClientInfoExtractor.fromRequestContext(rc),
        NexusExecutor.get()
    ).thenApply(ci -> {
      if (ci == null) {
        throw new ProblemDetailsException(
            new ProblemDetails.Single(
                ProblemDetailsTypes.SERVER_ERROR,
                "Internal Server Error",
                500,
                "Something went wrong",
                "/",
                Map.of()
            )
        );
      }

      return new Response<>(200, ci);
    });
  }
}

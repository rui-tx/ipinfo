package io.github.ruitx;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import org.nexus.RequestContext;
import org.nexus.Response;
import org.nexus.annotations.Mapping;
import org.nexus.annotations.RequestContextParam;
import org.nexus.enums.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

  private final Service service;

  @Inject
  public Controller(Service service) {
    this.service = service;
  }

  @Mapping(type = HttpMethod.GET, endpoint = "/")
  public CompletableFuture<Response<ClientInfo>> ipReport(@RequestContextParam RequestContext rc) {
    return service.ipReport(rc);
  }
}

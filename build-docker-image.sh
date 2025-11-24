#!/bin/sh

# Extract the main project version from pom.xml
VERSION=$(sed -n 's:.*<version>\(.*\)</version>.*:\1:p' pom.xml | head -n 1)

if [ -z "$VERSION" ]; then
  echo "Version not found in pom.xml"
  exit 1
fi

BINARY_NAME="ipinfo-$VERSION"
ARTIFACT_ID="ipinfo"

# Generate Dockerfile with dynamic binary name
cat > Dockerfile <<EOF
FROM vegardit/graalvm-maven:latest-java25 as builder

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# download dependencies, this layer will be cached
RUN mvn compile -B

# copy the rest of the source code
COPY . .

# Run native compilation
RUN mvn package -Pnative -DskipTests

FROM debian:bookworm-slim

LABEL io.github.ruitx."$ARTIFACT_ID".version="$VERSION"

WORKDIR /app

# Install runtime dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Copy the built native executable from the builder stage
COPY --from=builder /app/target/native/$BINARY_NAME /app/$ARTIFACT_ID
COPY --from=builder /app/.env /app/.env
#COPY --from=builder /app/migrations /app/migrations

# Set executable permission
RUN chmod +x /app/$ARTIFACT_ID

EXPOSE 15000

ENTRYPOINT ["/app/$ARTIFACT_ID"]
EOF

echo "Dockerfile generated with version: $VERSION"

# Build and tag
podman build --no-cache -t "$ARTIFACT_ID":"$VERSION" .
podman tag "$ARTIFACT_ID":"$VERSION" "$ARTIFACT_ID":latest

echo "Image generated with version: $VERSION"
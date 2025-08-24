#!/bin/bash
set -e

if [ -n "$PLUGIN_COMPONENTS" ]; then
  echo "Components specified: $PLUGIN_COMPONENTS"
  cd /moqui-framework
  # Fetch each component
  for component in $(echo "$PLUGIN_COMPONENTS" | tr ',' ' '); do
    echo "Fetching component: $component"
    ./gradlew getComponent -Pcomponent=$component
  done

  echo "Running addRuntime..."
  ./gradlew addRuntime

  echo "Extracting moqui-plus-runtime.war to /moqui-deploy..."
  cd /moqui-deploy
  jar -xvf /moqui-framework/moqui-plus-runtime.war
  cd /moqui-framework
else
  echo "No PLUGIN_COMPONENTS specified. Skipping component setup."
fi

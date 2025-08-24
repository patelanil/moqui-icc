FROM 600735190428.dkr.ecr.us-east-1.amazonaws.com/maarg-base-os:OS-22.04-latest

# Argument for git credentials
ARG GIT_USERNAME
ARG GIT_PASSWORD

# Configure git credentials
RUN echo -e "machine git.hotwax.co\nlogin $GIT_USERNAME\npassword $GIT_PASSWORD" > /root/.netrc

# Clone the necessary repositories in one RUN command to reduce image layers
WORKDIR /
RUN git clone --depth 1 -b "v3.3.0" https://github.com/hotwax/moqui-framework.git
WORKDIR /moqui-framework
RUN git clone --depth 1 -b "v3.2.0" https://github.com/hotwax/moqui-runtime.git runtime
WORKDIR /moqui-framework/runtime/component
RUN git clone --depth 1 -b "v1.2.3" https://github.com/hotwax/moqui-sftp.git
RUN git clone --depth 1 -b "v1.0.4" https://github.com/moqui/moqui-kie.git
RUN git clone --depth 1 -b "v1.1.4" https://github.com/hotwax/moqui-fop.git
RUN git clone --depth 1 -b "v2.5.3" https://github.com/hotwax/mantle-shopify-connector.git
RUN git clone --depth 1 -b "v1.9.0" https://github.com/hotwax/OrderRouting.git
RUN git clone --depth 1 -b "v2.2.1" https://github.com/hotwax/available-to-promise-maarg.git
RUN git clone --depth 1 -b "v2.6.0" https://github.com/hotwax/ofbiz-oms-udm.git
RUN git clone --depth 1 -b "v2.2.2" https://github.com/hotwax/inventory-count-maarg.git
RUN git clone --depth 1 -b "v1.4.0" https://github.com/hotwax/oms-bi.git
RUN git clone --depth 1 -b "v1.1.4" https://github.com/hotwax/shopify-delivery-rate.git
RUN git clone --depth 1 -b "v1.5.7" https://github.com/hotwax/oms.git
RUN git clone --depth 1 -b "v1.6.3" https://github.com/hotwax/mantle-netsuite-connector.git

RUN git clone --depth 1 -b "v2.4.6" https://git.hotwax.co/HC2/plugins/maarg-util.git
RUN git clone --depth 1 -b "v1.1.0" https://git.hotwax.co/tools/maargsetup.git
RUN git clone --depth 1 -b "v4.2.0" https://git.hotwax.co/HC2/maarg-docker-config.git
RUN git clone --depth 1 -b "v1.0.0" https://git.hotwax.co/HC2/plugins/erp-integration.git
RUN git clone --depth 1 -b "v1.5.9" https://git.hotwax.co/HC2/plugins/predictspring.git
RUN git clone --depth 1 -b "v3.4.4" https://git.hotwax.co/HC2/plugins/ofbiz-oms-usl.git
RUN git clone --depth 1 -b "v1.8.9" https://git.hotwax.co/HC2/shopify-oms-bridge.git
RUN git clone --depth 1 -b "v1.1.0" https://git.hotwax.co/HC2/plugins/gorgias.git
RUN git clone --depth 1 -b "v1.2.15" https://git.hotwax.co/HC2/poorti.git

WORKDIR /moqui-framework

RUN sed -ie 's|distributionUrl=[^ ]*|distributionUrl=gradle-7.4.1-bin.zip|g' gradle/wrapper/gradle-wrapper.properties
RUN cp runtime/component/maargsetup/gradle/gradle-7.4.1-bin.zip gradle/wrapper/

RUN echo "org.gradle.daemon=false" | tee -a gradle.properties


# Copy configuration files
RUN cp runtime/component/maarg-docker-config/docker/MoquiProductionConf.xml runtime/conf/
RUN cp runtime/component/maarg-docker-config/myaddons.xml myaddons.xml
RUN patch -p0 < runtime/component/maarg-docker-config/patches/MyAddons.patch
RUN cp runtime/component/maarg-util/shiro.ini framework/src/main/resources/
RUN mkdir -p framework/src/main/groovy/co/hotwax/auth/
RUN cp runtime/component/maarg-util/src/main/groovy/co/hotwax/auth/JWTManager.groovy framework/src/main/groovy/co/hotwax/auth/JWTManager.groovy
RUN cp runtime/component/maargsetup/conf/jwtKey.txt runtime/conf/
RUN cp runtime/component/maarg-util/src/main/resources/fop.xconf runtime/component/moqui-fop/src/main/resources/

RUN patch -p0 < runtime/component/maarg-util/patches/JwtToken.patch

#Enable compatibility between Moqui and OFBiz for encrypted fields
RUN mkdir -p framework/src/main/groovy/co/hotwax/util/
RUN patch -p0 < runtime/component/maarg-util/patches/EntityCrypto.patch
RUN cp runtime/component/maarg-util/src/main/groovy/co/hotwax/util/OmsEntityCrypto.groovy framework/src/main/groovy/co/hotwax/util/OmsEntityCrypto.groovy

# Create WAR file and extract it
RUN ./gradlew addRuntime
RUN mkdir -p /moqui-deploy && \
    cd /moqui-deploy && \
    jar -xvf /moqui-framework/moqui-plus-runtime.war

# Set environment variables
ENV DB_LOAD="./gradlew loadProduction -Ptypes=seed,seed-initial,install,ext-seed,ext -Praw"
ENV JAVA_OPTS="-Xms512m -Xmx4096m"
ENV CONF_FILE="/moqui-framework/runtime/conf/MoquiProductionConf.xml"
ENV SLEEP="sleep 2"

# Define additional environment variables for OFBiz and Moqui configuration
ENV OFBIZ_INSTANCE_NAME=""
ENV OFBIZ_INSTANCE_URL=""
ENV JWT_KEY=""
ENV Moqui_HOST=""
ENV Moqui_DB_HOST=""
ENV Moqui_DB_USER=""
ENV Moqui_DB_PASSWORD=""
ENV Moqui_DB_NAME=""
ENV MOQUI_REPLICA_DB_HOST=""
ENV MOQUI_REPLICA_DB_USER=""
ENV MOQUI_REPLICA_DB_PASSWORD=""
ENV MOQUI_REPLICA_DB_NAME=""
ENV Moqui_Bi_DB_Host=""
ENV Moqui_Bi_DB_User=""
ENV Moqui_Bi_DB_Password=""
ENV Moqui_Bi_DB_Name=""
ENV ELASTICSEARCH_HOST=""
ENV ELASTICSEARCH_USER=""
ENV ELASTICSEARCH_PASSWORD=""
ENV TIME_ZONE=""
ENV SCHEDULED_JOB_CHECK_TIME="60"
ENV PLUGIN_COMPONENTS=

WORKDIR /moqui-deploy
# Copy entrypoint script and set executable permissions
COPY docker/uat/entrypoint.sh .
COPY docker/uat/setup-components.sh .
RUN chmod +x entrypoint.sh setup-components.sh

# Expose the application port
EXPOSE 8080

# Define the entrypoint
ENTRYPOINT ["./entrypoint.sh", "&&", "bash"]

# Step-by-Step Guide to Set Up Maarg on Local Machine
This file outlines the steps to install and configure Maarg on a local machine.

## Prerequisites
- Java Development Kit (JDK 11): Minimum version required to run Moqui.
- Git: Version control system used for cloning repositories.
- MySQL 8: Database used in this guide (though Moqui supports others).

## Clone the Repositories
1. **Navigate to your working directory:** Open your terminal and use the `cd` command to navigate to your desired working directory. Replace `/your/working/directory` with the actual path.
   ``` 
   cd /your/working/directory 
   ```
2. **Clone Moqui Framework:** 
    Clone the Moqui framework repository from Github using the following command:
   ```
   git clone -b "main" https://github.com/hotwax/moqui-framework.git
   ```
3. **Navigate to Moqui Framework Directory:** 
   Change directory to the cloned Moqui framework directory:
   ```
   cd moqui-framework
   ```
4. **Clone Moqui Runtime:**
   Clone the Moqui runtime repository from Github:
   ```
   git clone -b "main" https://github.com/hotwax/moqui-runtime.git runtime
   ```
## Clone Additional Components
1. **Navigate to Components Directory:** 
    Change directory to the runtime/component directory:
   ```
   cd runtime/component
   ```
2. **Clone Additional Repositories: (Optional)** 
    Clone several additional component repositories using the following commands:
    ```
     git clone -b "main" https://github.com/hotwax/moqui-sftp.git
     git clone -b "v1.0.4" https://github.com/moqui/moqui-kie.git
     git clone -b "main" https://git.hotwax.co/HC2/plugins/maarg-util.git
     git clone -b "main" https://git.hotwax.co/HC2/maarg-docker-config.git
     git clone -b "main" https://github.com/hotwax/mantle-shopify-connector.git
     git clone -b "main" https://github.com/hotwax/OrderRouting.git
     git clone -b "main" https://github.com/hotwax/available-to-promise-maarg.git
     git clone -b "main" https://github.com/hotwax/ofbiz-oms-udm.git
     git clone -b "main" https://git.hotwax.co/HC2/plugins/ofbiz-oms-usl.git
     git clone -b "main" https://github.com/hotwax/moqui-fop.git
    ```
## Configure Moqui
1. **Update Database Entries:** 
    Edit the MoquiConf.xml file and add the following properties, replacing placeholders with your actual credentials:
    ```
    <default-property name="entity_ds_db_conf" value="mysql8"/>
    <default-property name="entity_ds_user" value="dbusername"/>
    <default-property name="entity_ds_password" value="dbpassword"/>
    <default-property name="entity_ds_database" value="dbname"/>
    
    <default-property name="ofbiz_entity_ds_db_conf" value="mysql8"/>
    <default-property name="ofbiz_entity_ds_host" value="localhost"/>
    <default-property name="ofbiz_entity_ds_user" value="ofbizdbusename"/>
    <default-property name="ofbiz_entity_ds_password" value="ofbizdbpassword"/>
    <default-property name="ofbiz_entity_ds_database" value="ofbizdbname"/>
    ```
   You can find the above property at ofbiz-oms-usl/MoquiConf.xml file.
2. **Data Load**
   Run the following command to load data:
   ```
   ./gradlew load -Ptypes=seed,seed-initial,install,ext-seed,ext -Praw
   ```
3. **Configure OpenSearch (Optional)**
   - **Download OpenSearch:** Use the following command to download OpenSearch:
     ```
     ./gradlew downloadOpenSearch
     ```
   - **Start OpenSearch Server:** Start the OpenSearch server using this command:
       ```
       ./gradlew startElasticSearch
       ```
4. **Run moqui server**
   ```
     ./gradlew run
   ```
## Visit the Application
   Open http://localhost:8080/ in your web browser to access the Maarg application.

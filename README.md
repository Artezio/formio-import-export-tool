# formio-io-tool

This application allows exporting forms from Formio server and importing them to the server again. It could be used for non-enterprise Formio servers for transfering forms between servers and repositories

### Requirements

* [Java] 1.8+
* [Maven] 3.6.1+
* [Git] 2.19.0+

### Installation

1. Clone this repository

    ```
    git clone https://github.com/Artezio/formio-import-export-tool.git
    ```

1. Build the application

    ```
    mvn clean install
    ```
    
### Usage in GUI mode    

1. To start in GUI mode, navigate to `target` directory and execute `java -jar import-export-tool.jar`

2. Fill in the server url, username and password for accessing the server.

3. Downloading forms from the server:
    * You can provide comma-separated list of tags to filter only forms with those tags
    * Specified directory will get filled with downloaded forms. If forms have slashes (`/`) in paths, then corresponding subdirectories will be created
    * Click `Action -> Download`  and wait for green or red indicator to appear, indicating success or failure correspondingly
    
4. Uploading forms to the server: 
    * Existing Forms will be overwritten
    * All uploaded forms will be accessible to `Administrator` user
    * All forms from specified directory will be uploaded, including subdirectories. If a form resites inside a subdirectory, its path will be constructed with forward slashes (e.g. if a form is located in `/a/b/form.json` file, then the form path will be set to `a/b/form.json` when uploaded to the server)
    * Click `Action -> Upload` and wait for green or red indicator to appear, indicating success or failure correspondingly

5. Deleting form from the server:
    * You can either provide comma-separated list of tags or comma-separated list of form paths to filter specific forms
    * If no tags or form paths are specified, all forms will be deleted from the server
    * Form paths have a precedence over tags
    * Click `Action -> Delete` and wait for green or red indicator to appear, indicating success or failure correspondingly
    
### Usage in command-line mode

Navigate to `target` directory and run `java -jar import-export-tool.jar -h` to get command-line help
    
[Java]: https://java.com
[Maven]: https://maven.apache.org/
[Git]: https://git-scm.com/

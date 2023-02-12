
# Payroll Processing System

The application is designed to serve over web APIs. The data needs to be uploaded to the web service as a MultiPart file in .csv format. In response, the data is processed and presented to the user according to predetermined conditions. In the design of the application, reading the information from the file, transforming it into an processable forum and processing it are separated from each other as different modules. These modules communicate with each other through the Pps module, which acts as a central hub. In case of any errors in the sent files, a logic has been applied that detects these errors, excludes them from the process and notifies the end user.

The application is developed in the Java(17) programming language. In order to send a request to application, the port number, base url which are specified in the application.properties file and api url should be used.
The api url is as follows: "api/v1/payrolls". POST request must be sent to this api for the request to be successful. Multiple files can be sent for processing. Files must be sent using the Request Parameter.

*Don't forget to adjust log url in the application.properties


## API Reference

#### Process Payrolls

```http
  Post /api/v1/reports
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `files` | `File` | Process payrolls in the files and show reports |



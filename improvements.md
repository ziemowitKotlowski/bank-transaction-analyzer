# Improvements

This file lists planned improvements for the project.

## Planned improvements

- **Implement a common interface and implementations for different import sources** - e.g. JSON file, XML file, etc.
- **Implement a common api contract for error responses** - for unified error handling across the application
- **Finish unit/integration tests** - for now, just a few tests are implemented
- **API versioning** - Mark current api as v1, to allow for future versions without breaking existing clients
- **Transaction date time zone issue** - As all of our transactionDocuments come from one user, the risk of importing dates from a different time zone is minimal, but possible. Consider enforcing a specific time zone for imported transaction dates.
- **New Relic dashboard** - Implement a New Relic dashboard to monitor application performance and error rates. Consider metrics like: durations (avg, p90, p95) of operations (e.g. imports), failure rate, memory usage, CPU usage.
- **Splunk integration** - For a complete deployment environment, a log aggregation tool like Splunk is needed
- **Upload import CSV files to S3** - Accept a csv from client, validate it, and upload to S3. This would allow reprocessing of failed imports without requiring the user to re-upload the file (reprocess in the background)
### CI/CD
- **Automated deployment pipeline** - Set up a CI/CD pipeline to automate testing and deployment processes, include steps like spotless apply to verify unified code format.

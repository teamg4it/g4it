_provide functional context for the application view optimization feature which contains - the current performance issues and the proposed solution.
current issue : currently we have one API endpoint (inventories/indicators/applications) that retrieves all the data for the application view, which can lead to performance issues and slow response times, especially when dealing with large datasets.
propose solution : We propose to optimize the application view by implementing a more efficient data retrieval strategy. This involves breaking down the data retrieval into smaller, more manageable API endpoints that can fetch only the necessary data based on user interactions and filters applied in the application view. By doing so, we can reduce the amount of data being transferred at once, leading to faster response times and improved performance.
Technical plan : 
1. Implement the aggregated criteria data into a separate API endpoint (inventories/indicators/applications/multi-criteria-impacts) that retrieves only the necessary criteria data based on user interactions and filters applied in the application view. This will allow for more efficient data retrieval and reduce the load on the main API endpoint. Additionally, we will implement caching mechanisms to store frequently accessed data, further improving response times. The new endpoints will be designed to handle pagination and filtering, ensuring that only relevant data is returned to the client. Overall, this solution aims to enhance the performance of the application view by optimizing data retrieval and reducing response times for users.
2. Implement new API for Multi-criteria data retrieval (inventories/indicators/applications/multi-criteria) that allows users to fetch data based on multiple criteria simultaneously along with current selection or current graph where graph tree structure is muliti criteria/domain level/sub-domain level/application level/VM level. This will enable users to customize their data views more effectively and reduce the need for multiple API calls, further improving performance. The new endpoint will support advanced filtering options and return only the relevant data based on the specified criteria, selected repartition option (like lifecycle, environment, equipment etc) options, ensuring that users receive the most pertinent information without unnecessary overhead.
3. An existing applications API endpoint (inventories/indicators/applications) will be optimized to work in conjunction with the new endpoints. This may involve refactoring the endpoint to support pagination for table view data for applications, implementing additional caching mechanisms, and ensuring compatibility with the new criteria and multi-criteria endpoints. The goal is to maintain the functionality of the existing endpoint while improving overall performance and response times.
4. For multi-criteria endpoint (inventories/indicators/applications/multi-criteria) We need few additional parameters along with the one which you generated in query to send response to UI that is mentioned below.
Total parameteres need to add in response for every graph view selection (domain, sub-domain, application, vm) are as follows:
* when user click on global level graph view, we need to send the domain wise data to the UI. This will allow users to see the overall impact of their selections and provide a comprehensive view of the data. The response domain wise data need is -
  - total impact and SIP for each domain
  - number of subdomains for each domain
  - number of applications for each domain
* when user click on a specific domain level graph, we need to send subdomain wise data to the UI. This will enable users to drill down into specific domains and understand their performance and impact in detail. The response subdomain wise data need is -
  - total impact and SIP for each subdomain
  - number of applications for each subdomainin
* when user click on a specific subdomain level graph, we need to send application wise data to the UI. This will allow users to analyze the performance and impact of individual applications within a subdomain. The response application wise data need is -
  - total impact and SIP for each application
* when user click on a specific application level graph, we need to send VM wise data to the UI. This will enable users to understand the performance and impact of individual VMs within an application. The response VM wise data need is -
  - total impact and SIP for each VM
  - cluster name for each VM
  - equipment name for each VM
  - environment name for each VM

describe the solution with spec documentation:_
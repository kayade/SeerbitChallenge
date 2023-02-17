# SeerbitChallenge
 
 INTRODUCTION
 
 This is RESTful API for transaction statistics. The purpose for the 
 API is to calculate real time statistics for the last 30 seconds of transactions.
 
 POST /transaction: called every time a transaction is made.
 GET /transaction: returns the statistic based on the transactions of the last 30 seconds
 DELETE /transaction: deletes all transactions
 
 USAGE
 
 To build run the following command in your terminal: mvn clean install
 
 To run the application run the following command in your terminal: mvn clean package exec:java
 
 After running the application use the following URLS to perform POST, DELETE, AND GET operations:
 
 HTTP - POST : http://localhost:5645/transactions
 HTTP - DELETE : http://localhost:5645/transactions
 HTTP - GET : http://localhost:5645/statistics
 
 HTTP - POST : https://localhost:5646/transactions
 HTTP - DELETE : https://localhost:5646/transactions
 HTTP - GET : https://localhost:5646/statistics
 
 

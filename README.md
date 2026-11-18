RETAIL MANAGEMENT SYSTEM

Folder layout:
retail/
  *.java
  schema.sql
  README.md
  out/          compiled .class files go here

Database defaults:
  URL:      jdbc:mysql://localhost:3306/retail_db
  Username: root
  Password: 1604daksh

1) Run schema.sql in MySQL Workbench.
2) Keep mysql-connector-j-26.7.0.jar in the folder ABOVE retail.
3) Open PowerShell in that parent folder and compile:
   javac -cp "mysql-connector-j-26.7.0.jar" -d retail/out retail/*.java
4) Run:
   java -cp "retail/out;mysql-connector-j-26.7.0.jar" retail.MainApp

All Java source files are directly inside retail. There is no src folder.
Compiled files are kept in retail/out so the project folder stays tidy.

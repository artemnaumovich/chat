# Multi-User Chat
This is a simple chat that is written on Java with using sockets
## Requirements
You need to have installed JDK on your machine.
## Installation
1. Clone the repository
   ```sh
   git clone https://github.com/artemnaumovich/chat.git
   ```
## Usage
1. Navigate to the folder with source code
   ```sh
   cd src
   ```
2. Compile the files by running the commands
   ```sh
   javac Server.java
   javac Client.java
   ```
3. Run Server
   ```sh
   java Server 'port'
   ```
   For example
   ```sh
   java Server 8585
   ```
4. Run Client
   ```sh
   java Client 'host' 'port'
   ```
   For example
   ```sh
   java Client 127.0.0.1 8585
   ```
5. Enter your name and click `join`

   ![Join example](/assets/join.png?raw=true "Join example")
6. Сhat with other users

   ![Chat example](/assets/chat.png?raw=true "Chat example")

[1.1]: http://i.imgur.com/tXSoThF.png (twitter icon with padding)
[1]: https://twitter.com/WestworldWyatt
# [Wyatt](https://www.mtheory7.com/)
[![alt text][1.1]][1]

Above is a link to the current status of Wyatt running connected to my personal Binance account.

Another link to the full log (all levels) is available [here](https://www.mtheory7.com/full.php)
### Architecture
The bot itself is kicked off by a Spring Boot Application. The Spring App also exposes some endpoints that can interact and use the bot's functions. The available endpoints will be described in the API section. These endpoints will then be used for a web UI showing more informative feedback about the bot's operation and status. 
### API
To get the current bot's status (main UI):
```$xslt
GET: http://host-ip:port/status
```
To get the current bot's BTC balance:
```$xslt
GET: http://host-ip:port/balance/btc
```
To get the current bot's profit (%):
```$xslt
GET: http://host-ip:port/balance/profit
```
To get the current bot's open orders:
```$xslt
GET: http://host-ip:port/orders
```
To shutdown the bot:
```$xslt
GET: http://host-ip:port/seppuku?pass={password}
```
### Logic
  * Gather recent data using [Binance-API](https://github.com/binance-exchange/binance-java-api)
  * Use data to find averages for various time intervals
  * Predict the next selling price using calculated targets
  * When current price is above target sell price, execute sell
  * Then, execute buy back immediately after for a configured percentage below the targeted sell price
  * Once decided, Wyatt sells at the current price not the target sell price
### Building
First clone and build the [Binance-API](https://github.com/binance-exchange/binance-java-api) repository to install the necessary packages into your local Maven repository (it is needed to build Wyatt)
  
To build Wyatt clone this repository and on the same level as pom.xml, execute 
```$xslt
mvn clean install
```
This will build and package two .jar files in the target directory. One of the .jar files end with "-jar-with-dependencies.jar" and includes all the libraries needed for independent execution.
### Executing
To run Wyatt, execute the following
```$xslt
java -jar target/wyatt-{VERSION}.jar <arg1> <arg2> <arg3> <arg4> <arg5> <arg6>
```
 * arg1 = Binance API Key*
 * arg2 = Binance Secret Key*
 * arg3 = Twitter OAuth Consumer Key
 * arg4 = Twitter OAuth Consumer Secret
 * arg5 = Twitter OAuth Access Token
 * arg6 = Twitter OAuth Access Token Secret

#### Executing without Twitter
If you desire to not use the tweeting feature of the bot, simply pass in only the two Binance keys necessary to trade!
```$xslt
java -jar target/wyatt-{VERSION}.jar <arg1> <arg2>
```
 * arg1 = Binance API Key*
 * arg2 = Binance Secret Key*
 
 *The Binance API Key absolutely MUST have approval to execute trades from Binance, but does not need approval to withdraw.
cd C:\Vishal\Projects\FirmFinancials
set PATH=C:\Program Files\Java\jdk1.5.0_07\bin
set CLASSPATH=.;C:\Vishal\Projects\Spider
javac -Xlint StockQuoter.java FirmDataCollector.java FirmDataParser.java PortfolioDAO.java  ..\Spider\Spiderable.java ..\Spider\Spider.java ..\Spider\HrefParser.java

java FirmDataCollector
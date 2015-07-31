cd C:\Vishal\Projects\FirmFinancials
set PATH=C:\Program Files\Java\jdk1.5.0_07\bin
set CLASSPATH=.;C:\Vishal\Projects\Spider
javac -Xlint execution\RelativeAnalysis.java  dao\PortfolioDAO.java  ..\Spider\Spiderable.java ..\Spider\Spider.java ..\Spider\HrefParser.java

java execution.RelativeAnalysis
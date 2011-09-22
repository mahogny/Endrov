nba <- read.csv("http://datasets.flowingdata.com/ppg2008.csv", sep=",")

nba <- nba[order(nba$PTS),]




#install.packages("ape")
library("ape")

tre = read.tree("~/expsummary-0-60.1/phylip/apt-pearson/outtree2")

#tre = ladderize(treorig)

hresult = as.hclust(tre)
plot(hresult, hang = -1)


#library(help=ape)



#dd 
library(FD)
dd <- gowdis(dummy$trait)  # can get in other ways
hresult <- hclust(dd)
plot(hresult, hang = -1)
dendro <- as.dendrogram(hresult)
par(lwd = 2.5, cex = 1.5)
plot(dendro, horiz = TRUE)





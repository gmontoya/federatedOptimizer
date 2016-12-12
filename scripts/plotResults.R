#!/usr/bin/env Rscript

require(stringr)

myfunction <- function(x) {
    z <- ""
    if ( x <= 0 ) {
        z <- "TIMEOUT"
    }
    return(z)
}

require(ggplot2)
library(scales)
library(grid)


dataOur <-read.table("outputExecuteOurQueriesWithProxies")
dataFedX <- read.table("outputExecuteQueries")
dataVoid <- read.table("outputExecuteQueriesVOID")
dataSplendid <- read.table("outputExecuteQueriesSPLENDIDWithProxies")

dataOur$approach <- "OUR"
dataFedX$approach <- "FedX"
dataVoid$approach <- "VOID"
dataSplendid$approach <- "Splendid"

colnames(dataOur) <-  c("Q", "PT", "ET", "NTT", "NC", "NR", "approach")
colnames(dataVoid) <-  c("Q", "PT", "ET", "NTT", "NC", "NR", "approach")
colnames(dataFedX) <-  c("Q", "ET", "NTT", "NC", "NR", "approach")
colnames(dataSplendid) <- c("Q", "ET", "NTT", "NC", "NR", "approach")

myvars <- c("Q", "ET", "NTT", "approach")

newDataOur <- dataOur[myvars]
newDataFedX <- dataFedX[myvars]
newDataVoid <- dataVoid[myvars]
newDataSplendid <- dataSplendid[myvars]

total <- rbind(newDataOur, newDataFedX, newDataVoid, newDataSplendid)

total["ET"] <- sapply(total[, "ET"], function(x) if (x < 1800000) { x } else { 0 })

sqs <- 1
theme_set(theme_gray(base_size = 32))
dev=pdf("ET_log.pdf", height=8, width=24)
p <- ggplot(total, aes(x=Q, y=ET, fill=approach)) + scale_y_continuous(trans = log10_trans(), breaks = trans_breaks("log10", function(x) 100^x), labels = trans_format("log10", math_format(10^.x)))
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="") 
p <- p + geom_text(aes(Q, y = 10, size = 12, label=sapply(total$ET, myfunction), angle = 90), position = position_dodge(width = 1), show.legend = FALSE)
p <- p + scale_fill_manual(values=c("#fbb4ae","#b3cde3","#ccebc5","#decbe4"))
p <- p + theme(axis.title.x = element_blank()) + ylab("Execution Time (ms)")
p
dev.off()

theme_set(theme_gray(base_size = 32))
dev=pdf("NTT_log.pdf", height=8, width=24)
p <- ggplot(total, aes(x=Q, y=NTT, fill=approach)) + scale_y_continuous(trans = log10_trans(), breaks = trans_breaks("log10", function(x) 100^x), labels = trans_format("log10", math_format(10^.x)))
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="") 
p <- p + scale_fill_manual(values=c("#fbb4ae","#b3cde3","#ccebc5","#decbe4"))
p <- p + theme(axis.title.x = element_blank()) + ylab("Number of Transferred Tuples")
p
dev.off()

theme_set(theme_gray(base_size = 32))
dev=pdf("ET.pdf", height=8, width=24)
p <- ggplot(total, aes(x=Q, y=ET, fill=approach))
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="")
p <- p + geom_text(aes(Q, y = 50000, size = 12, label=sapply(total$ET, myfunction), angle = 90), position = position_dodge(width = 1), show.legend = FALSE) 
p <- p + scale_fill_manual(values=c("#fbb4ae","#b3cde3","#ccebc5","#decbe4"))
p <- p + theme(axis.title.x = element_blank()) + ylab("Execution Time (ms)")
p
dev.off()

theme_set(theme_gray(base_size = 32))
dev=pdf("NTT.pdf", height=8, width=24)
p <- ggplot(total, aes(x=Q, y=NTT, fill=approach))
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="") 
p <- p + scale_fill_manual(values=c("#fbb4ae","#b3cde3","#ccebc5","#decbe4"))
p <- p + theme(axis.title.x = element_blank()) + ylab("Number of Transferred Tuples")
p
dev.off()

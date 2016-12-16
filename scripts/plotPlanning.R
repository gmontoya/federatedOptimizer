#!/usr/bin/env Rscript

require(stringr)

myfunction <- function(x) {
    z <- ""
    if ( x <= 0 ) {
        z <- "NO PLAN"
    }
    return(z)
}

require(ggplot2)
library(scales)
library(grid)


dataOur <-read.table("planningOur")
dataFedXCold <- read.table("planningFedXCold")
dataFedXWarm <- read.table("planningFedXWarm")
dataVoid <- read.table("planningVoid")
dataSplendid <- read.table("planningSplendid")

dataOur$approach <- "Odyssey"
dataFedXCold$approach <- "FedX-cold"
dataFedXWarm$approach <- "FedX-warm"
dataVoid$approach <- "DP-VOID"
dataSplendid$approach <- "Splendid"

colnames(dataOur) <-  c("Q", "PT", "NSS", "approach")
colnames(dataVoid) <-  c("Q", "PT", "NSS", "approach")
colnames(dataFedXCold) <-  c("Q", "PT", "NSS", "approach")
colnames(dataFedXWarm) <-  c("Q", "PT", "NSS", "approach")
colnames(dataSplendid) <- c("Q", "PT", "NSS", "approach")

total <- rbind(dataOur, dataFedXCold, dataFedXWarm, dataVoid, dataSplendid)

total$approach <- factor(total$approach, c("Odyssey", "FedX-cold", "FedX-warm", "Splendid", "DP-VOID"))

total$Q <- factor(total$Q, c("LD1", "LD2", "LD3", "LD4", "LD5", "LD6", "LD7"    , "LD8", "LD9", "LD10", "LD11", "CD1", "CD2", "CD3", "CD4", "CD5", "CD6", "CD7", "LS1", "LS2", "LS3", "LS4", "LS5", "LS6", "LS7"))

sqs <- 1
theme_set(theme_gray(base_size = 32))
dev=pdf("PT.pdf", height=8, width=24)
p <- ggplot(total, aes(x=Q, y=PT, fill=approach)) 
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="") 
p <- p + theme(axis.title.x = element_blank()) + ylab("Optimization Time (ms)")
p
dev.off()

theme_set(theme_gray(base_size = 32))
dev=pdf("PT_log.pdf", height=8, width=24)
p <- ggplot(total, aes(x=Q, y=PT, fill=approach)) + scale_y_continuous(trans = log10_trans(), breaks = trans_breaks("log10", function(x) 100^x), labels = trans_format("log10", math_format(10^.x)))
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="") 
p <- p + theme(axis.title.x = element_blank()) + ylab("Optimization Time (ms)")
p
dev.off()

theme_set(theme_gray(base_size = 32))
dev=pdf("NSS.pdf", height=8, width=24)
p <- ggplot(total, aes(x=Q, y=NSS, fill=approach)) 
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="") 
p <- p + geom_text(aes(Q, y = 50, size = 12, label=sapply(total$NSS, myfunction), angle = 90), position = position_dodge(width = 1), show.legend = FALSE) 
p <- p + theme(axis.title.x = element_blank()) + ylab("# Sub-queries")
p
dev.off()

theme_set(theme_gray(base_size = 32))
dev=pdf("NSS_log.pdf", height=8, width=24)
p <- ggplot(total, aes(x=Q, y=NSS, fill=approach)) + scale_y_continuous(trans = log10_trans(), breaks = trans_breaks("log10", function(x) 100^x), labels = trans_format("log10", math_format(10^.x)))
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="") 
p <- p + geom_text(aes(Q, y = 5, size = 12, label=sapply(total$NSS, myfunction), angle = 90), position = position_dodge(width = 1), show.legend = FALSE) 
p <- p + theme(axis.title.x = element_blank()) + ylab("# Sub-queries")
p
dev.off()

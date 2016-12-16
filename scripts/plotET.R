#!/usr/bin/env Rscript

## Gives count, mean, standard deviation, standard error of the mean, and confidence interval (default 95%).
##   data: a data frame.
##   measurevar: the name of a column that contains the variable to be summariezed
##   groupvars: a vector containing names of columns that contain grouping variables
##   na.rm: a boolean that indicates whether to ignore NA's
##   conf.interval: the percent range of the confidence interval (default is 95%)
summarySE <- function(data=NULL, measurevar, groupvars=NULL, na.rm=FALSE,
                      conf.interval=.95, .drop=TRUE) {
    library(plyr)

    # New version of length which can handle NA's: if na.rm==T, don't count them
    length2 <- function (x, na.rm=FALSE) {
        if (na.rm) sum(!is.na(x))
        else       length(x)
    }

    # This does the summary. For each group's data frame, return a vector with
    # N, mean, and sd
    datac <- ddply(data, groupvars, .drop=.drop,
      .fun = function(xx, col) {
        c(N    = length2(xx[[col]], na.rm=na.rm),
          mean = mean   (xx[[col]], na.rm=na.rm),
          sd   = sd     (xx[[col]], na.rm=na.rm)
        )
      },
      measurevar
    )

    # Rename the "mean" column    
    datac <- rename(datac, c("mean" = measurevar))

    datac$se <- datac$sd / sqrt(datac$N)  # Calculate standard error of the mean

    # Confidence interval multiplier for standard error
    # Calculate t-statistic for confidence interval: 
    # e.g., if conf.interval is .95, use .975 (above/below), and use df=N-1
    ciMult <- qt(conf.interval/2 + .5, datac$N-1)
    datac$ci <- datac$se * ciMult

    return(datac)
}

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


dataOur <-read.table("outputExecuteOurQueries")
dataFedX <- read.table("outputExecuteQueries")
dataVoid <- read.table("outputExecuteQueriesVOID")
dataSplendid <- read.table("outputExecuteQueriesSPLENDID")

dataOur <- dataOur[-c(1, 11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111, 121, 131, 141, 151, 161, 171, 181, 191, 201, 211, 221, 231, 241), ]
dataFedX <- dataFedX[-c(1, 11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111, 121, 131, 141, 151, 161, 171, 181, 191, 201, 211, 221, 231, 241), ]
dataVoid <- dataVoid[-c(1, 11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111, 121, 131, 141, 151, 161, 171, 181, 191, 201, 211, 221, 231, 241), ]
dataSplendid <- dataSplendid[-c(1, 11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111, 121, 131, 141, 151, 161, 171, 181, 191, 201, 211, 221, 231, 241), ]

dataOur$approach <- "Odyssey"
dataFedX$approach <- "FedX"
dataVoid$approach <- "DP-VOID"
dataSplendid$approach <- "Splendid"

colnames(dataOur) <-  c("Q", "PT", "ET", "NR", "approach")
colnames(dataVoid) <-  c("Q", "PT", "ET", "NR", "approach")
colnames(dataFedX) <-  c("Q", "ET", "NTT", "approach")
colnames(dataSplendid) <- c("Q", "ET", "NTT", "approach")

myvars <- c("Q", "ET", "approach")

newDataOur <- dataOur[myvars]
newDataFedX <- dataFedX[myvars]
newDataVoid <- dataVoid[myvars]
newDataSplendid <- dataSplendid[myvars]

newDataOur$Q <- factor(newDataOur$Q, c("LD1", "LD2", "LD3", "LD4", "LD5", "LD6", "LD7", "LD8", "LD9", "LD10", "LD11", "CD1", "CD2", "CD3", "CD4", "CD5", "CD6", "CD7", "LS1", "LS2", "LS3", "LS4", "LS5", "LS6", "LS7"))
newDataFedX$Q <- factor(newDataFedX$Q, c("LD1", "LD2", "LD3", "LD4", "LD5", "LD6", "LD7", "LD8", "LD9", "LD10", "LD11", "CD1", "CD2", "CD3", "CD4", "CD5", "CD6", "CD7", "LS1", "LS2", "LS3", "LS4", "LS5", "LS6", "LS7"))
newDataVoid$Q <- factor(newDataVoid$Q, c("LD1", "LD2", "LD3", "LD4", "LD5", "LD6", "LD7", "LD8", "LD9", "LD10", "LD11", "CD1", "CD2", "CD3", "CD4", "CD5", "CD6", "CD7", "LS1", "LS2", "LS3", "LS4", "LS5", "LS6", "LS7"))
newDataSplendid$Q <- factor(newDataSplendid$Q, c("LD1", "LD2", "LD3", "LD4", "LD5", "LD6", "LD7", "LD8", "LD9", "LD10", "LD11", "CD1", "CD2", "CD3", "CD4", "CD5", "CD6", "CD7", "LS1", "LS2", "LS3", "LS4", "LS5", "LS6", "LS7"))

total <- rbind(newDataOur, newDataFedX, newDataVoid, newDataSplendid)
total$approach <- factor(total$approach, c("Odyssey", "FedX", "Splendid", "DP-VOID"))

total$Q <- factor(total$Q, c("LD1", "LD2", "LD3", "LD4", "LD5", "LD6", "LD7", "LD8", "LD9", "LD10", "LD11", "CD1", "CD2", "CD3", "CD4", "CD5", "CD6", "CD7", "LS1", "LS2", "LS3", "LS4", "LS5", "LS6", "LS7"))

totalc <- summarySE(total, measurevar="ET", groupvars=c("Q","approach"))

totalc["ET"] <- sapply(totalc[, "ET"], function(x) if (x < 1800000) { x } else { 0 })

sqs <- 1
theme_set(theme_gray(base_size = 32))
dev=pdf("ET_log.pdf", height=8, width=24)
p <- ggplot(totalc, aes(x=Q, y=ET, fill=approach)) + scale_y_continuous(trans = log10_trans(), breaks = trans_breaks("log10", function(x) 100^x), labels = trans_format("log10", math_format(10^.x)))
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="") 
p <- p + geom_text(aes(Q, y = 10, size = 12, label=sapply(totalc$ET, myfunction), angle = 90), position = position_dodge(width = 1), show.legend = FALSE)
#p <- p + scale_fill_manual(values=c("#fbb4ae","#b3cde3","#ccebc5","#decbe4"))
p <- p + theme(axis.title.x = element_blank()) + ylab("Execution Time (ms)")
p
dev.off()

theme_set(theme_gray(base_size = 32))
dev=pdf("ET.pdf", height=8, width=24)
p <- ggplot(totalc, aes(x=Q, y=ET, fill=approach))
p <- p + geom_bar(stat="identity",position="dodge") 
p <- p + theme(panel.background = element_rect(fill = 'white', colour = 'black')) 
p <- p + theme(panel.grid.major.y = element_line(colour = "gray80"))
p <- p + theme(legend.position = "top", legend.box = "horizontal", legend.key.size= unit(sqs, "cm"))+ labs(fill="")
p <- p + geom_text(aes(Q, y = 50000, size = 12, label=sapply(totalc$ET, myfunction), angle = 90), position = position_dodge(width = 1), show.legend = FALSE) 
#p <- p + scale_fill_manual(values=c("#fbb4ae","#b3cde3","#ccebc5","#decbe4"))
p <- p + theme(axis.title.x = element_blank()) + ylab("Execution Time (ms)")
p
dev.off()

